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
 * @author jiangwei (ydswcy513@gmail.com)
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
	 * 准备一个新连接的方法
	 * 
	 * @return 如果启动统计 返回获取连接的时间
	 * @throws ThriftConnectionPoolException
	 *             当获取连接出现问题时抛出该异常
	 */
	protected long preConnection() throws ThriftConnectionPoolException {
		long statsObtainTime = 0;

		if (this.pool.poolShuttingDown) {
			throw new ThriftConnectionPoolException(this.pool.shutdownStackTrace);
		}

		return statsObtainTime;
	}

	/**
	 * 获取连接后执行其他任务
	 * 
	 * @param handle
	 *            连接代理类
	 * @param statsObtainTime
	 *            准备获取连接的时间
	 */
	protected void postConnection(ThriftConnectionHandle<T> handle, long statsObtainTime) {

		handle.renewConnection(); // 重置连接代理类中的信息 变成新连接

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

	/*
	 * @see
	 * com.wmz7year.thrift.pool.ThriftConnectionStrategy#getConnection(byte[])
	 */
	@Override
	public ThriftConnection<T> getConnection(byte[] nodeID) throws ThriftConnectionPoolException {
		long statsObtainTime = preConnection();

		ThriftConnectionHandle<T> result = (ThriftConnectionHandle<T>) getConnectionInternal(nodeID);
		if (result != null) {
			postConnection(result, statsObtainTime);
		}

		return result;
	}

	/**
	 * 获取连接代理对象的方法
	 * 
	 * @return thrift服务器连接对象
	 * @throws ThriftConnectionPoolException
	 *             当获取连接出现问题时抛出该异常
	 */
	protected abstract ThriftConnection<T> getConnectionInternal() throws ThriftConnectionPoolException;

	/**
	 * 使用指定服务器节点获取连接代理对象的方法
	 * 
	 * @param nodeID
	 *            thrift服务器节点ID
	 * @return thrift服务器连接对象
	 * @throws ThriftConnectionPoolException
	 *             当获取连接出现问题时抛出该异常
	 */
	protected abstract ThriftConnection<T> getConnectionInternal(byte[] nodeID) throws ThriftConnectionPoolException;

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
