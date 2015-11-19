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

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wmz7year.thrift.pool.config.ThriftServerInfo;
import com.wmz7year.thrift.pool.connection.ThriftConnection;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/**
 * thrift连接代理类<br>
 * 
 * 
 * @Title: ThriftConnectionHandle.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 上午11:40:26
 * @version V1.0
 */
public class ThriftConnectionHandle<T extends TServiceClient> implements ThriftConnection<T>, Serializable {
	protected static Logger logger = LoggerFactory.getLogger(ThriftConnectionHandle.class);
	private static final long serialVersionUID = 8927450495285911268L;

	/**
	 * 连接所在的分区
	 */
	private ThriftConnectionPartition<T> thriftConnectionPartition;
	/**
	 * thrift服务器信息
	 */
	private ThriftServerInfo thriftServerInfo;
	/**
	 * 连接超时时间
	 */
	private long connectionTimeout;
	/**
	 * 连接最大存活时间
	 */
	protected long maxConnectionAgeInMs;
	/**
	 * 连接代理类持有的真实连接对象
	 */
	private ThriftConnection<T> thriftConnection;

	/** 是否调用关闭连接方法的表识位 */
	protected AtomicBoolean logicallyClosed = new AtomicBoolean();

	/**
	 * 连接池对象
	 */
	private ThriftConnectionPool<T> thriftConnectionPool;

	/** 连接最后使用时间 */
	private long connectionLastUsedInMs;
	/** 连接最后重置时间 */
	private long connectionLastResetInMs;
	/** 连接创建时间 */
	protected long connectionCreationTimeInMs;
	/** 连接是否损坏的标识位 */
	protected boolean possiblyBroken;
	/** 使用该连接的线程 */
	protected Thread threadUsingConnection;

	/**
	 * 连接监控线程
	 */
	private volatile Thread threadWatch;

	public ThriftConnectionHandle(ThriftConnection<T> thriftConnection,
			ThriftConnectionPartition<T> thriftConnectionPartition, ThriftConnectionPool<T> thriftConnectionPool,
			boolean recreating) throws ThriftConnectionPoolException {
		// 判断是否是新连接
		boolean newConnection = thriftConnection == null;

		if (!recreating) {
			connectionLastUsedInMs = System.currentTimeMillis();
			connectionLastResetInMs = System.currentTimeMillis();
			connectionCreationTimeInMs = System.currentTimeMillis();
		}

		this.thriftConnectionPool = thriftConnectionPool;
		this.thriftConnectionPartition = thriftConnectionPartition;
		this.thriftServerInfo = thriftConnectionPartition.getThriftServerInfo();
		this.connectionTimeout = thriftConnectionPool.getConfig().getConnectTimeout();
		this.connectionTimeout = thriftConnectionPool.getConfig().getMaxConnectionAge(TimeUnit.MILLISECONDS);

		this.maxConnectionAgeInMs = thriftConnectionPool.getConfig().getMaxConnectionAge(TimeUnit.MILLISECONDS);

		try {
			this.thriftConnection = newConnection ? thriftConnectionPool.obtainInternalConnection(this)
					: thriftConnection;
		} catch (ThriftConnectionPoolException e) {
			possiblyBroken = true;
			throw e;
		}
	}

	/*
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			if (this.logicallyClosed.compareAndSet(false, true)) {

				// 中断连接监视线程
				if (this.threadWatch != null) {
					this.threadWatch.interrupt();
					this.threadWatch = null;
				}

				ThriftConnectionHandle<T> handle = null;
				try {
					handle = this.recreateConnectionHandle();
					this.thriftConnectionPool.connectionStrategy.cleanupConnection(this, handle);
					this.thriftConnectionPool.releaseConnection(handle);
				} catch (ThriftConnectionPoolException e) {
					possiblyBroken = true;
					// 检查连接是否关闭了
					if (!isClosed()) {
						this.thriftConnectionPool.connectionStrategy.cleanupConnection(this, handle);
						this.thriftConnectionPool.releaseConnection(this);
					}
					throw new IOException(e);
				}
			} else {
				this.thriftConnectionPool.postDestroyConnection(this);
			}
		} catch (Exception e) {
			possiblyBroken = true;
			throw new IOException(e);
		}
		if (thriftConnection != null) {
			thriftConnection.close();
		}
	}

	/**
	 * 创建新的连接代理对象<br>
	 * 用于清理可能存在的奇奇怪怪配置
	 * 
	 * @return ConnectionHandle 新创建的连接对象
	 * @throws ThriftConnectionPoolException
	 *             当产生错误时抛出该异常
	 */
	public ThriftConnectionHandle<T> recreateConnectionHandle() throws ThriftConnectionPoolException {
		ThriftConnectionHandle<T> handle = new ThriftConnectionHandle<T>(this.thriftConnection,
				this.thriftConnectionPartition, this.thriftConnectionPool, true);
		handle.thriftConnectionPartition = this.thriftConnectionPartition;
		handle.connectionCreationTimeInMs = this.connectionCreationTimeInMs;
		handle.connectionLastResetInMs = this.connectionLastResetInMs;
		handle.connectionLastUsedInMs = this.connectionLastUsedInMs;
		handle.possiblyBroken = this.possiblyBroken;
		this.thriftConnection = null;

		return handle;
	}

	/*
	 * @see com.wmz7year.thrift.pool.connection.ThriftConnection#getClient()
	 */
	@Override
	public T getClient() {
		if (thriftConnection != null) {
			return thriftConnection.getClient();
		}
		throw new IllegalStateException("连接代理类没有绑定的原始连接信息");
	}

	/**
	 * 获取连接代理对象绑定的原始连接的方法
	 * 
	 * @return 原始连接对象
	 */
	public ThriftConnection<T> getInternalConnection() {
		return this.thriftConnection;
	}

	/**
	 * 设置代理对象绑定的原始连接的方法
	 * 
	 * @param thriftConnection
	 *            原始连接对象
	 */
	public void setInternalConnection(ThriftConnection<T> thriftConnection) {
		this.thriftConnection = thriftConnection;
	}

	/**
	 * 获取连接代理对象所对应的thrift服务器信息对象
	 * 
	 * @return thrift服务器信息对象
	 */
	public ThriftServerInfo getThriftServerInfo() {
		return this.thriftServerInfo;
	}

	/**
	 * 获取连接代理类所在的分区对象
	 * 
	 * @return 分区对象
	 */
	public ThriftConnectionPartition<T> getConnectionPartition() {
		return this.thriftConnectionPartition;
	}

	/**
	 * 设置连接代理类所属的连接分区的方法
	 * 
	 * @param thriftConnectionPartition
	 *            连接分区对象
	 */
	public void setOriginatingPartition(ThriftConnectionPartition<T> thriftConnectionPartition) {
		this.thriftConnectionPartition = thriftConnectionPartition;
	}

	/**
	 * 获取连接超时时间的方法
	 * 
	 * @return 连接超时时间
	 */
	public long getConnectionTimeout() {
		return this.connectionTimeout;
	}

	/**
	 * 关闭代理对象中的原始连接的方法
	 */
	public void internalClose() throws ThriftConnectionPoolException {
		try {
			if (this.thriftConnection != null) {
				this.thriftConnection.close();
			}
			logicallyClosed.set(true);
		} catch (IOException e) {
			throw new ThriftConnectionPoolException(e);
		}
	}

	/*
	 * @see com.wmz7year.thrift.pool.connection.ThriftConnection#isClosed()
	 */
	@Override
	public boolean isClosed() {
		return this.logicallyClosed.get();
	}

	/**
	 * 判断连接是否已经过期的方法
	 * 
	 * @return true为已过期 false为未过期
	 */
	public boolean isExpired() {
		return this.maxConnectionAgeInMs > 0 && isExpired(System.currentTimeMillis());
	}

	/**
	 * 使用指定的时间判断连接是否过期的方法
	 * 
	 * @param currentTime
	 *            指定的时间戳
	 * @return true为已过期 false为未过期
	 */
	protected boolean isExpired(long currentTime) {
		return this.maxConnectionAgeInMs > 0
				&& (currentTime - this.connectionCreationTimeInMs) > this.maxConnectionAgeInMs;
	}

	/**
	 * 判断连接是否出现问题的方法
	 * 
	 * @return true为发生过异常 false为未发生异常
	 */
	public boolean isPossiblyBroken() {
		return this.possiblyBroken;
	}

	/**
	 * 更新最后重置时间的方法
	 * 
	 * @param connectionLastReset
	 *            最后的重置时间
	 */
	public void setConnectionLastResetInMs(long connectionLastReset) {
		this.connectionLastResetInMs = connectionLastReset;
	}

	/**
	 * 设置连接最后使用的时间
	 * 
	 * @param connectionLastUsed
	 *            连接最后使用的时间
	 */
	public void setConnectionLastUsedInMs(long connectionLastUsed) {
		this.connectionLastUsedInMs = connectionLastUsed;
	}

	/**
	 * 逻辑上重新打开连接
	 */
	public void renewConnection() {
		this.logicallyClosed.set(false);
		this.threadUsingConnection = Thread.currentThread();
	}

	/**
	 * 获取连接的创建时间
	 * 
	 * @return 连接的创建时间
	 */
	public long getConnectionCreationTimeInMs() {
		return this.connectionCreationTimeInMs;
	}

	/**
	 * 获取连接最后的使用时间
	 * 
	 * @return 连接最后的使用时间
	 */
	public long getConnectionLastUsedInMs() {
		return this.connectionLastUsedInMs;
	}

	/**
	 * 获取连接最后重置时间的方法
	 * 
	 * @return 连接最后重置时间
	 */
	public long getConnectionLastResetInMs() {
		return this.connectionLastResetInMs;
	}

}
