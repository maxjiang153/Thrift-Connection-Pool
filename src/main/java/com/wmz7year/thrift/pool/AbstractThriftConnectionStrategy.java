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

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.thrift.TServiceClient;

import com.wmz7year.thrift.pool.connection.ThriftConnection;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/**
 * 连接操作策略接口抽象实现类
 * 
 * @Title: AbstractThriftConnectionStrategy.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 下午3:43:58
 * @version V1.0
 */
public abstract class AbstractThriftConnectionStrategy<T extends TServiceClient>
		implements ThriftConnectionStrategy<T>, Serializable {
	private static final long serialVersionUID = 8901255269360914953L;
	/**
	 * 连接池对象
	 */
	protected ThriftConnectionPool<T> pool;

	/**
	 * 关闭锁，防止一次性关闭所有连接时出现问题
	 */
	protected Lock terminationLock = new ReentrantLock();

	/**
	 * Prep for a new connection
	 * 
	 * @return if stats are enabled, return the nanoTime when this connection
	 *         was requested.
	 * @throws SQLException
	 */
	protected long preConnection() throws ThriftConnectionPoolException {
		long statsObtainTime = 0;

		if (this.pool.poolShuttingDown) {
			throw new ThriftConnectionPoolException(this.pool.shutdownStackTrace);
		}

		return statsObtainTime;
	}

	/**
	 * After obtaining a connection, perform additional tasks.
	 * 
	 * @param handle
	 * @param statsObtainTime
	 */
	protected void postConnection(ThriftConnectionHandle<T> handle, long statsObtainTime) {

		handle.renewConnection(); // mark it as being logically "open"

	}

	/*
	 * @see com.wmz7year.thrift.pool.ThriftConnectionStrategy#getConnection()
	 */
	@Override
	public ThriftConnection<T> getConnection() throws ThriftConnectionPoolException {
		long statsObtainTime = preConnection();

		ThriftConnectionHandle<T> result = (ThriftConnectionHandle<T>) getConnectionInternal();
		if (result != null) {
			postConnection(result, statsObtainTime);
		}

		return result;
	}

	/**
	 * Actual call that returns a connection
	 * 
	 * @return Connection
	 * @throws SQLException
	 */
	protected abstract ThriftConnection<T> getConnectionInternal() throws ThriftConnectionPoolException;

	/*
	 * @see
	 * com.wmz7year.thrift.pool.ThriftConnectionStrategy#cleanupConnection(com.
	 * wmz7year.thrift.pool.ThriftConnectionHandle,
	 * com.wmz7year.thrift.pool.ThriftConnectionHandle)
	 */
	@Override
	public void cleanupConnection(ThriftConnectionHandle<T> oldHandler, ThriftConnectionHandle<T> newHandler) {
		// do nothing
	}

}
