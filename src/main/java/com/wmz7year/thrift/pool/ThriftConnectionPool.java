/**
 *  				Copyright 2015 Jiang Wei
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.wmz7year.thrift.pool;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.ServiceOrder;
import com.wmz7year.thrift.pool.config.ThriftServerInfo;
import com.wmz7year.thrift.pool.connection.ThriftConnection;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/**
 * thrift连接池主类
 * 
 * @Title: ThriftConnectionPool.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 上午9:54:53
 * @version V1.0
 */
public class ThriftConnectionPool<T extends TServiceClient> implements Serializable, Closeable {
	private static final Logger logger = LoggerFactory.getLogger(ThriftConnectionPool.class);
	private static final long serialVersionUID = 6524222103868846620L;

	/**
	 * 连接池配置对象SphinxConnectionPool
	 */
	private ThriftConnectionPoolConfig config;

	/**
	 * 连接超时时间
	 */
	private int connectionTimeOut;

	/**
	 * 配置的服务器列表
	 */
	private List<ThriftServerInfo> thriftServers;

	/**
	 * 服务器数量
	 */
	private int thriftServerCount = 0;

	/**
	 * 用于异步方式获取连接的服务
	 */
	private ListeningExecutorService asyncExecutor;

	/**
	 * 用于保持连接，定时执行连接上的某个方法
	 */
	protected ScheduledExecutorService keepAliveScheduler;
	/**
	 * 处理连接最大存活时间的定时器
	 */
	private ScheduledExecutorService maxAliveScheduler;
	/**
	 * 监听每个服务器上的连接<br>
	 * 检查是需要动态创建新的连接还是关闭多余的连接
	 */
	private ExecutorService connectionsScheduler;

	/**
	 * 保存分区的连接信息
	 */
	private List<ThriftConnectionPartition<T>> partitions;

	/**
	 * 构造器
	 * 
	 * @param config
	 *            连接池配置对象
	 * @throws ThriftConnectionPoolException
	 *             当发生错误的时候抛出该异常信息
	 */
	public ThriftConnectionPool(ThriftConnectionPoolConfig config) throws ThriftConnectionPoolException {
		this.config = config;
		// TODO check 配置
		this.connectionTimeOut = this.config.getConnectTimeout();

		// 获取配置的服务器列表
		this.thriftServers = this.config.getThriftServers();
		this.thriftServerCount = this.thriftServers.size();

		// 判断是否是懒加载 如果是则验证连接
		if (!this.config.isLazyInit()) {
			// 需要删除的服务器列表
			List<ThriftServerInfo> needToDelete = new ArrayList<ThriftServerInfo>();

			// 尝试获取一个连接
			for (int i = 0; i < thriftServerCount; i++) {
				ThriftServerInfo thriftServerInfo = thriftServers.get(i);
				try {
					ThriftConnection<T> connection = obtainRawInternalConnection(thriftServerInfo);
					connection.close();
				} catch (Exception e) {
					needToDelete.add(thriftServerInfo);
					logger.error("无法从服务器 " + thriftServerInfo.toString() + "中获取连接 将移除该服务器");
				}
			}

			// 删除服务器信息
			for (ThriftServerInfo thriftServerInfo : needToDelete) {
				thriftServers.remove(thriftServerInfo);
			}

			// 移除完毕检查数量
			thriftServerCount = thriftServers.size();
			if (thriftServerCount == 0) {
				throw new ThriftConnectionPoolException("无可以thrift服务器，连接池启动失败");
			}
		}

		// TODO 连接追踪？
		this.asyncExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

		// 设置线程池名称
		String suffix = "";
		if (this.config.getPoolName() != null) {
			suffix = "-" + this.config.getPoolName();
		}

		// 创建连接池 FIXME 目前最大的监控池为100个thrift服务 已经满足绝大多数需求 可以考虑修改为动态的线程池大小
		this.keepAliveScheduler = Executors.newScheduledThreadPool(100,
				new CustomThreadFactory("ThriftConnectionPool-keep-alive-scheduler" + suffix, true));
		this.maxAliveScheduler = Executors.newScheduledThreadPool(100,
				new CustomThreadFactory("ThriftConnectionPool-max-alive-scheduler" + suffix, true));
		this.connectionsScheduler = Executors.newFixedThreadPool(100,
				new CustomThreadFactory("ThriftConnectionPoolP-pool-watch-thread" + suffix, true));

		// 创建分区列表
		this.partitions = new ArrayList<ThriftConnectionPartition<T>>(thriftServerCount);

		// TODO 其他配置

		// 队列模式
		ServiceOrder serviceOrder = this.config.getServiceOrder();

		// 根据服务器配置创建不同的连接分区
		for (int p = 0; p < thriftServerCount; p++) {
			ThriftConnectionPartition<T> thriftConnectionPartition = new ThriftConnectionPartition<T>(this,
					thriftServers.get(p));
			this.partitions.add(thriftConnectionPartition);
			// 添加空闲连接队列
			BlockingQueue<ThriftConnectionHandle<T>> connectionHandles = new LinkedBlockingQueue<ThriftConnectionHandle<T>>(
					this.config.getMaxConnectionPerServer());
			this.partitions.get(p).setFreeConnections(connectionHandles);

			if (!this.config.isLazyInit()) {
				for (int i = 0; i < this.config.getMinConnectionPerServer(); i++) {
					// 初始化连接代理对象
					this.partitions.get(p).addFreeConnection(
							new ThriftConnectionHandle<T>(null, this.partitions.get(p), this, false));
				}

			}

			// 连接过期时间监控
			if (this.config.getIdleConnectionTestPeriod(TimeUnit.SECONDS) > 0
					|| this.config.getIdleMaxAge(TimeUnit.SECONDS) > 0) {

				final Runnable connectionTester = new ThriftConnectionTesterThread<T>(thriftConnectionPartition, this,
						this.config.getIdleMaxAge(TimeUnit.MILLISECONDS),
						this.config.getIdleConnectionTestPeriod(TimeUnit.MILLISECONDS), serviceOrder);
				long delayInSeconds = this.config.getIdleConnectionTestPeriod(TimeUnit.SECONDS);
				if (delayInSeconds == 0L) {
					delayInSeconds = this.config.getIdleMaxAge(TimeUnit.SECONDS);
				}
				if (this.config.getIdleMaxAge(TimeUnit.SECONDS) < delayInSeconds
						&& this.config.getIdleConnectionTestPeriod(TimeUnit.SECONDS) != 0
						&& this.config.getIdleMaxAge(TimeUnit.SECONDS) != 0) {
					delayInSeconds = this.config.getIdleMaxAge(TimeUnit.SECONDS);
				}
				this.keepAliveScheduler.scheduleAtFixedRate(connectionTester, delayInSeconds, delayInSeconds,
						TimeUnit.SECONDS);
			}

			// 连接最长存活时间监控
			if (this.config.getMaxConnectionAgeInSeconds() > 0) {
				final Runnable connectionMaxAgeTester = new ThriftConnectionMaxAgeThread<T>(thriftConnectionPartition,
						this, this.config.getMaxConnectionAge(TimeUnit.MILLISECONDS), serviceOrder);
				this.maxAliveScheduler.scheduleAtFixedRate(connectionMaxAgeTester,
						this.config.getMaxConnectionAgeInSeconds(), this.config.getMaxConnectionAgeInSeconds(),
						TimeUnit.SECONDS);
			}
			// 连接数量监控
			this.connectionsScheduler.execute(new PoolWatchThread<T>(thriftConnectionPartition, this));
		}
	}

	/**
	 * 根据配置获取原始连接的方法
	 * 
	 * @param serverInfo
	 *            thrift服务器信息
	 * @return thrift客户端连接对象
	 * @throws ThriftConnectionPoolException
	 *             当获取连接出现问题时抛出该异常
	 */
	private ThriftConnection<T> obtainRawInternalConnection(ThriftServerInfo serverInfo)
			throws ThriftConnectionPoolException {
		// TODO get connection
		return null;
	}

	/*
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * 从连接池中获取一个连接的方法
	 * 
	 * @return 连接对象
	 * @throws ThriftConnectionPoolException
	 *             当获取连接出现错误时抛出该异常
	 */
	public T getConnection() throws ThriftConnectionPoolException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 获取连接池配置对象的方法
	 * 
	 * @return 连接池配置对象
	 */
	public ThriftConnectionPoolConfig getConfig() {
		return this.config;
	}

	/**
	 * 获取一个thrift原始连接对象的方法
	 * 
	 * @param thriftConnectionHandle
	 * @return
	 * @throws ThriftConnectionPoolException
	 */
	public ThriftConnection<T> obtainInternalConnection(ThriftConnectionHandle<T> thriftConnectionHandle)
			throws ThriftConnectionPoolException {
		boolean tryAgain = false;
		ThriftConnection<T> result = null;
		ThriftConnection<T> oldRawConnection = thriftConnectionHandle.getInternalConnection();
		ThriftServerInfo thriftServerInfo = thriftConnectionHandle.getThriftServerInfo();

		int acquireRetryAttempts = this.getConfig().getAcquireRetryAttempts();
		long acquireRetryDelayInMs = this.getConfig().getAcquireRetryDelayInMs();
		AcquireFailConfig acquireConfig = new AcquireFailConfig();
		acquireConfig.setAcquireRetryAttempts(new AtomicInteger(acquireRetryAttempts));
		acquireConfig.setAcquireRetryDelayInMs(acquireRetryDelayInMs);
		acquireConfig.setLogMessage("Failed to acquire connection to " + thriftServerInfo.toString());

		do {
			result = null;

			try {
				// 尝试获取原始连接
				result = this.obtainRawInternalConnection(thriftServerInfo);
				tryAgain = false;

				if (acquireRetryAttempts != this.getConfig().getAcquireRetryAttempts()) {
					logger.info("Successfully re-established connection to " + thriftServerInfo.toString());
				}

				thriftConnectionHandle.getConnectionPartition().getServerIsDown().set(false);

				thriftConnectionHandle.setInternalConnection(result);

			} catch (ThriftConnectionPoolException e) {
				logger.error(String.format("Failed to acquire connection to %s. Sleeping for %d ms. Attempts left: %d",
						thriftServerInfo.toString(), acquireRetryDelayInMs, acquireRetryAttempts), e);

				try {
					if (acquireRetryAttempts > 0) {
						Thread.sleep(acquireRetryDelayInMs);
					}
					tryAgain = (acquireRetryAttempts--) > 0;
				} catch (InterruptedException e1) {
					tryAgain = false;
				}

				if (!tryAgain) {
					if (oldRawConnection != null) {
						try {
							oldRawConnection.close();
						} catch (IOException e1) {
							throw new ThriftConnectionPoolException(e1);
						}
					}
					thriftConnectionHandle.setInternalConnection(oldRawConnection);
					throw e;
				}

			}
		} while (tryAgain);
		return result;
	}

}
