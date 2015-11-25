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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.ServiceOrder;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.ThriftServiceType;
import com.wmz7year.thrift.pool.config.ThriftServerInfo;
import com.wmz7year.thrift.pool.connection.DefaultThriftConnection;
import com.wmz7year.thrift.pool.connection.MulitServiceThriftConnecion;
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
	protected List<ThriftServerInfo> thriftServers;

	/**
	 * 服务器数量
	 */
	protected int thriftServerCount = 0;

	/**
	 * 用于异步方式获取连接的服务
	 */
	private ListeningExecutorService asyncExecutor;

	/**
	 * 保存分区的连接信息
	 */
	protected List<ThriftConnectionPartition<T>> partitions;

	/**
	 * 判断连接池是否在关闭过程中的表识位
	 */
	protected volatile boolean poolShuttingDown;

	/**
	 * 当分区连接小于x%的时候触发创建连接信号
	 */
	protected final int poolAvailabilityThreshold;

	/**
	 * 连接获取策略处理类
	 */
	protected ThriftConnectionStrategy<T> connectionStrategy;
	/**
	 * 关闭连接池的原因
	 */
	protected String shutdownStackTrace;
	/**
	 * 连接获取超时时间
	 */
	protected long connectionTimeoutInMs;

	/**
	 * 连接创建统计锁
	 */
	protected ReentrantReadWriteLock serverListLock = new ReentrantReadWriteLock();

	/**
	 * thrift服务类型 单服务还是多服务
	 */
	protected ThriftServiceType thriftServiceType;

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
		// 检查配置内容是否正确
		this.config.check();
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
			if (thriftServerCount == 0 && !this.config.isNoServerStartUp()) {
				throw new ThriftConnectionPoolException("无可用thrift服务器，连接池启动失败");
			}
		}

		this.asyncExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

		// 创建线程调度引擎
		TaskEngine.getInstance();

		// 创建分区列表
		this.partitions = new ArrayList<ThriftConnectionPartition<T>>(thriftServerCount);

		this.poolAvailabilityThreshold = this.config.getPoolAvailabilityThreshold();
		this.connectionStrategy = new DefaultThriftConnectionStrategy<T>(this);
		this.connectionTimeoutInMs = this.config.getConnectionTimeoutInMs();
		this.thriftServiceType = this.config.getThriftServiceType();

		if (this.connectionTimeoutInMs == 0) {
			this.connectionTimeoutInMs = Long.MAX_VALUE;
		}

		// 根据服务器配置创建不同的连接分区
		for (int p = 0; p < thriftServerCount; p++) {
			ThriftServerInfo thriftServerInfo = thriftServers.get(p);
			ThriftConnectionPartition<T> thriftConnectionPartition = createThriftConnectionPartition(thriftServerInfo);
			partitions.add(thriftConnectionPartition);
		}
	}

	/**
	 * 添加新的thrift服务器的方法
	 * 
	 * @param thriftServerInfo
	 *            需要添加的服务器信息
	 * @return true为添加成功 false为添加失败
	 */
	public boolean addThriftServer(ThriftServerInfo thriftServerInfo) throws ThriftConnectionPoolException {
		serverListLock.writeLock().lock();
		try {
			// 验证服务器是否可用
			try {
				ThriftConnection<T> connection = obtainRawInternalConnection(thriftServerInfo);
				connection.close();
			} catch (Exception e) {
				throw new ThriftConnectionPoolException(e);
			}
			ThriftConnectionPartition<T> thriftConnectionPartition = createThriftConnectionPartition(thriftServerInfo);

			partitions.add(thriftConnectionPartition);
			thriftServers.add(thriftServerInfo);
			thriftServerCount = partitions.size();
		} finally {
			serverListLock.writeLock().unlock();
		}
		return true;
	}

	/**
	 * 移除现有thrift服务器的方法<br>
	 * 注意 如果没有可用的thrift则无法获取到连接
	 * 
	 * @param thriftServerInfo
	 *            需要删除的服务器信息
	 * @return true为删除成功 false为删除失败
	 */
	public boolean removeThriftServer(ThriftServerInfo thriftServerInfo) {
		// 不存在这台服务器 直接返回失败
		if (!thriftServers.contains(thriftServerInfo)) {
			return false;
		}

		serverListLock.writeLock().lock();
		try {
			Iterator<ThriftConnectionPartition<T>> iterator = partitions.iterator();
			while (iterator.hasNext()) {
				ThriftConnectionPartition<T> thriftConnectionPartition = iterator.next();
				if (thriftConnectionPartition.getThriftServerInfo().equals(thriftServerInfo)) {
					thriftConnectionPartition.setUnableToCreateMoreTransactions(false);
					List<ThriftConnectionHandle<T>> clist = new LinkedList<ThriftConnectionHandle<T>>();
					thriftConnectionPartition.getFreeConnections().drainTo(clist);
					for (ThriftConnectionHandle<T> c : clist) {
						destroyConnection(c);
					}
					thriftConnectionPartition.stopThreads();
					partitions.remove(thriftConnectionPartition);
					thriftServers.remove(thriftServerInfo);
					thriftServerCount = partitions.size();
					return true;
				}
			}
		} finally {
			serverListLock.writeLock().unlock();
		}
		return false;
	}

	/**
	 * 根据配置信息对象创建连接分区的方法
	 * 
	 * @param thriftServerInfo
	 *            配置信息对象
	 * @return 连接分区对象
	 * @throws ThriftConnectionPoolException
	 *             创建连接分区过程中可能产生的异常
	 */
	private ThriftConnectionPartition<T> createThriftConnectionPartition(ThriftServerInfo thriftServerInfo)
			throws ThriftConnectionPoolException {
		// 队列模式
		ServiceOrder serviceOrder = this.config.getServiceOrder();

		ThriftConnectionPartition<T> thriftConnectionPartition = new ThriftConnectionPartition<T>(this,
				thriftServerInfo);
		// 添加空闲连接队列
		BlockingQueue<ThriftConnectionHandle<T>> connectionHandles = new LinkedBlockingQueue<ThriftConnectionHandle<T>>(
				this.config.getMaxConnectionPerServer());
		thriftConnectionPartition.setFreeConnections(connectionHandles);

		if (!this.config.isLazyInit()) {
			for (int i = 0; i < this.config.getMinConnectionPerServer(); i++) {
				// 初始化连接代理对象
				thriftConnectionPartition
						.addFreeConnection(new ThriftConnectionHandle<T>(null, thriftConnectionPartition, this, false));
			}

		}

		// 连接过期时间监控
		if (this.config.getIdleConnectionTestPeriod(TimeUnit.SECONDS) > 0
				|| this.config.getIdleMaxAge(TimeUnit.SECONDS) > 0) {

			final TimerTask connectionTester = new ThriftConnectionTesterThread<T>(thriftConnectionPartition, this,
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
			TaskEngine.getInstance().scheduleAtFixedRate(connectionTester, delayInSeconds, delayInSeconds);
		}

		// 连接最长存活时间监控
		if (this.config.getMaxConnectionAgeInSeconds() > 0) {
			final TimerTask connectionMaxAgeTester = new ThriftConnectionMaxAgeThread<T>(thriftConnectionPartition,
					this, this.config.getMaxConnectionAge(TimeUnit.MILLISECONDS), serviceOrder);
			TaskEngine.getInstance().scheduleAtFixedRate(connectionMaxAgeTester,
					this.config.getMaxConnectionAgeInSeconds(), this.config.getMaxConnectionAgeInSeconds());
		}
		// 连接数量监控
		TaskEngine.getInstance().submit(new PoolWatchThread<T>(thriftConnectionPartition, this));

		return thriftConnectionPartition;

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
		// 判断单服务还是多服务模式
		ThriftConnection<T> connection = null;
		if (this.thriftServiceType == ThriftServiceType.SINGLE_INTERFACE) {
			connection = new DefaultThriftConnection<T>(serverInfo.getHost(), serverInfo.getPort(),
					this.connectionTimeOut, this.config.getThriftProtocol(), this.config.getClientClass());
		} else {
			connection = new MulitServiceThriftConnecion<T>(serverInfo.getHost(), serverInfo.getPort(),
					this.connectionTimeOut, this.config.getThriftProtocol(), this.config.getThriftClientClasses());
		}
		return connection;
	}

	/*
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		shutdown();
	}

	/**
	 * 关闭连接池的方法
	 */
	public synchronized void shutdown() {
		if (!this.poolShuttingDown) {
			logger.info("开始关闭thrift连接池...");
			this.poolShuttingDown = true;
			this.shutdownStackTrace = captureStackTrace("开始关闭thrift连接池");

			TaskEngine.getInstance().shutdown();

			this.asyncExecutor.shutdownNow();

			try {
				this.asyncExecutor.awaitTermination(5, TimeUnit.SECONDS);

			} catch (InterruptedException e) {
				// do nothing
			}

			this.connectionStrategy.terminateAllConnections();
			logger.info("关闭thrift连接池完成");
		}
	}

	/**
	 * 获取方法调用堆栈信息的方法
	 * 
	 * @param message
	 *            提示语
	 * @return 堆栈信息
	 */
	protected String captureStackTrace(String message) {
		StringBuilder stringBuilder = new StringBuilder(String.format(message, Thread.currentThread().getName()));
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for (int i = 0; i < trace.length; i++) {
			stringBuilder.append(' ').append(trace[i]).append("\r\n");
		}

		stringBuilder.append("");

		return stringBuilder.toString();
	}

	/**
	 * 从连接池中获取一个连接的方法
	 * 
	 * @return 连接对象
	 * @throws ThriftConnectionPoolException
	 *             当获取连接出现错误时抛出该异常
	 */
	public ThriftConnection<T> getConnection() throws ThriftConnectionPoolException {
		return this.connectionStrategy.getConnection();
	}

	/**
	 * 使用指定服务器ID从连接池中获取一个连接的方法
	 * 
	 * @param nodeID
	 *            服务器节点的ID
	 * @return 连接对象
	 * @throws ThriftConnectionPoolException
	 *             当获取连接出现错误时抛出该异常
	 */
	public ThriftConnection<T> getConnection(byte[] nodeID) throws ThriftConnectionPoolException {
		return this.connectionStrategy.getConnection(nodeID);
	}

	/**
	 * 异步获取连接的方法
	 * 
	 * @return 连接代理对象
	 */
	public ListenableFuture<ThriftConnection<T>> getAsyncConnection() {
		return this.asyncExecutor.submit(new Callable<ThriftConnection<T>>() {

			public ThriftConnection<T> call() throws Exception {
				return getConnection();
			}
		});
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

	/**
	 * 将连接返回到连接池中的方法<br>
	 * 该方法在调用connection.close()的时候触发<br>
	 * 但是连接并不会真正的关掉 而是清理后返回连接池中
	 * 
	 * @param connection
	 *            需要回收的连接对象
	 * @throws ThriftConnectionPoolException
	 *             回收过程中可能产生的异常
	 */
	public void releaseConnection(ThriftConnection<T> connection) throws ThriftConnectionPoolException {
		ThriftConnectionHandle<T> handle = (ThriftConnectionHandle<T>) connection;

		// 判断连接池是否是在关闭中 如果不是在关闭中则执行回收操作
		if (!this.poolShuttingDown) {
			internalReleaseConnection(handle);
		}
	}

	/**
	 * 将连接代理对象回收到连接池中的方法
	 * 
	 * @param handle
	 *            连接代理对象
	 * @throws ThriftConnectionPoolException
	 *             回收过程中可能产生的异常
	 */
	protected void internalReleaseConnection(ThriftConnectionHandle<T> connectionHandle)
			throws ThriftConnectionPoolException {
		if (connectionHandle.isExpired() || (!this.poolShuttingDown && connectionHandle.isPossiblyBroken()
				&& !isConnectionHandleAlive(connectionHandle))) {

			if (connectionHandle.isExpired()) {
				connectionHandle.internalClose();
			}

			ThriftConnectionPartition<T> connectionPartition = connectionHandle.getConnectionPartition();
			postDestroyConnection(connectionHandle);

			maybeSignalForMoreConnections(connectionPartition);
			return;
		}
		connectionHandle.setConnectionLastUsedInMs(System.currentTimeMillis());
		if (!this.poolShuttingDown) {
			putConnectionBackInPartition(connectionHandle);
		} else {
			connectionHandle.internalClose();
		}
	}

	/**
	 * 将连接放回对应分区的方法
	 * 
	 * @param connectionHandle
	 *            连接代理对象
	 * @throws ThriftConnectionPoolException
	 *             回收连接过程出现错误时抛出该异常
	 */
	protected void putConnectionBackInPartition(ThriftConnectionHandle<T> connectionHandle)
			throws ThriftConnectionPoolException {
		BlockingQueue<ThriftConnectionHandle<T>> queue = connectionHandle.getConnectionPartition().getFreeConnections();
		if (!queue.offer(connectionHandle)) { // 如果无法放回则关闭
			connectionHandle.internalClose();
		}
	}

	/**
	 * 判断连接代理类是否可用的方法
	 * 
	 * @param connection
	 *            需要检测的连接代理类对象
	 * @return true为可用 false为不可用
	 */
	public boolean isConnectionHandleAlive(ThriftConnectionHandle<T> connection) {
		boolean result = false;
		boolean logicallyClosed = connection.logicallyClosed.get();
		try {
			connection.logicallyClosed.compareAndSet(true, false);

			// 反射调用ping方法
			T client = null;
			if (this.thriftServiceType == ThriftServiceType.SINGLE_INTERFACE) {
				client = connection.getClient();
			} else {
				Map<String, T> muiltServiceClients = connection.getMuiltServiceClients();
				if (muiltServiceClients.size() == 0) { // 没有可用的客户端 直接返回
					return false;
				}
				Iterator<T> iterator = muiltServiceClients.values().iterator();
				if (iterator.hasNext()) {
					client = iterator.next();
				} else {
					return false;
				}
			}
			try {
				Method method = client.getClass().getMethod("ping");
				method.invoke(client);
			} catch (Exception e) {
				return false;
			}

			result = true;
		} finally {
			connection.logicallyClosed.set(logicallyClosed);
			connection.setConnectionLastResetInMs(System.currentTimeMillis());
		}
		return result;
	}

	/**
	 * 准备销毁连接的方法<br>
	 * 通知连接分区准备创建新的连接
	 * 
	 * @param handle
	 *            需要销毁的连接地理对象
	 */
	protected void postDestroyConnection(ThriftConnectionHandle<T> handle) {
		ThriftConnectionPartition<T> partition = handle.getConnectionPartition();

		partition.updateCreatedConnections(-1);
		partition.setUnableToCreateMoreTransactions(false);
	}

	/**
	 * 检查连接分区的连接数量<br>
	 * 如果连接数量不足则向事件队列中添加新的创建连接信号
	 * 
	 * @param connectionPartition
	 *            需要检测的连接分区
	 */
	protected void maybeSignalForMoreConnections(ThriftConnectionPartition<T> connectionPartition) {
		if (!connectionPartition.isUnableToCreateMoreTransactions() && !this.poolShuttingDown
				&& connectionPartition.getAvailableConnections() * 100
						/ connectionPartition.getMaxConnections() <= this.poolAvailabilityThreshold) {
			connectionPartition.getPoolWatchThreadSignalQueue().offer(new Object());
		}
	}

	/**
	 * 销毁连接的方法
	 * 
	 * @param conn
	 *            需要销毁的连接代理对象
	 */
	protected void destroyConnection(ThriftConnectionHandle<T> conn) {
		postDestroyConnection(conn);
		try {
			conn.internalClose();
		} catch (ThriftConnectionPoolException e) {
			logger.error("Error in attempting to close connection", e);
		}
	}

	/**
	 * 销毁单个连接分区的方法
	 * 
	 * @param thriftConnectionPartition
	 *            需要销毁的连接分区对象
	 */
	public void destroyThriftConnectionPartition(ThriftConnectionPartition<T> thriftConnectionPartition) {
		try {
			serverListLock.writeLock().lock();
			// 首先移除分区信息
			partitions.remove(thriftConnectionPartition);
			thriftConnectionPartition.stopThreads();
			ThriftServerInfo thriftServerInfo = thriftConnectionPartition.getThriftServerInfo();
			thriftServers.remove(thriftServerInfo);
			thriftServerCount = partitions.size();
			logger.info("连接池移除服务器信息：" + thriftServerInfo.getHost() + " 端口:" + thriftServerInfo.getPort());
			if (getThriftServerCount() == 0) {
				logger.error("当前连接池中无可用服务器  无法获取新的客户端连接");
			}
		} finally {
			serverListLock.writeLock().unlock();
		}
		List<ThriftConnectionHandle<T>> clist = new LinkedList<ThriftConnectionHandle<T>>();
		thriftConnectionPartition.getFreeConnections().drainTo(clist);
		for (ThriftConnectionHandle<T> c : clist) {
			destroyConnection(c);
		}
	}

	/**
	 * 获取thrift服务器数量的方法
	 * 
	 * @return 服务器数量
	 */
	public int getThriftServerCount() {
		return thriftServerCount;
	}

}
