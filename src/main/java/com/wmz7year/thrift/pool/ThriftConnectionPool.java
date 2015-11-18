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
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig;
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
	private Collection<ThriftServerInfo> thriftServers;

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

		// 判断是否是懒加载 如果是则验证连接
		if (!this.config.isLazyInit()) {
			// 尝试获取一个连接
			try {
				ThriftConnection<T> connection = obtainRawInternalConnection();
				connection.close();
			} catch (Exception e) {
				throw new ThriftConnectionPoolException("尝试获取原始连接失败 无法从配置的thrift服务器列表中获取连接 启动连接池失败", e);
			}
		}

		// TODO 连接追踪？
		this.asyncExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

		this.thriftServers = this.config.getThriftServers();

		// 设置线程池名称
		String suffix = "";
		if (this.config.getPoolName() != null) {
			suffix = "-" + this.config.getPoolName();
		}

		// 创建连接池
		this.keepAliveScheduler = Executors.newScheduledThreadPool(this.thriftServers.size(),
				new CustomThreadFactory("ThriftConnectionPool-keep-alive-scheduler" + suffix, true));
		this.maxAliveScheduler = Executors.newScheduledThreadPool(this.thriftServers.size(),
				new CustomThreadFactory("ThriftConnectionPool-max-alive-scheduler" + suffix, true));
		this.connectionsScheduler = Executors.newFixedThreadPool(this.thriftServers.size(),
				new CustomThreadFactory("ThriftConnectionPoolP-pool-watch-thread" + suffix, true));

		this.thriftServerCount = this.thriftServers.size();

		// TODO 其他配置
	}

	/**
	 * 根据配置获取原始连接的方法
	 * 
	 * @return thrift客户端连接对象
	 */
	private ThriftConnection<T> obtainRawInternalConnection() {
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

}
