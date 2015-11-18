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
	 * 连接池对象
	 */
	private ThriftConnectionPool<T> thriftConnectionPool;

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

	public ThriftConnectionHandle(ThriftConnection<T> thriftConnection,
			ThriftConnectionPartition<T> thriftConnectionPartition, ThriftConnectionPool<T> thriftConnectionPool,
			boolean recreating) throws ThriftConnectionPoolException {
		// 判断是否是新连接
		boolean newConnection = thriftConnection == null;

		this.thriftConnectionPartition = thriftConnectionPartition;
		this.thriftConnectionPool = thriftConnectionPool;
		this.thriftServerInfo = thriftConnectionPartition.getThriftServerInfo();
		this.connectionTimeout = thriftConnectionPool.getConfig().getConnectTimeout();
		this.connectionTimeout = thriftConnectionPool.getConfig().getMaxConnectionAge(TimeUnit.MILLISECONDS);

		try {
			this.thriftConnection = newConnection ? thriftConnectionPool.obtainInternalConnection(this)
					: thriftConnection;
		} catch (ThriftConnectionPoolException e) {
			throw e;
		}
	}

	/*
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public T getClient() {
		// TODO Auto-generated method stub
		return null;
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

}
