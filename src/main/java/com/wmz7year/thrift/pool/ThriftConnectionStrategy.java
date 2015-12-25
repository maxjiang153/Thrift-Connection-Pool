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

import org.apache.thrift.TServiceClient;

import com.wmz7year.thrift.pool.connection.ThriftConnection;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/**
 * 连接操作策略接口
 * 
 * @author jiangwei (ydswcy513@gmail.com)
 * @version V1.0
 */
public interface ThriftConnectionStrategy<T extends TServiceClient> {

	/**
	 * 获取一个thrift连接的方法
	 * 
	 * @return thrift连接代理对象
	 * @throws ThriftConnectionPoolException
	 *             当获取连接出现问题的时候抛出该异常
	 */
	public ThriftConnection<T> getConnection() throws ThriftConnectionPoolException;

	/**
	 * 使用指定服务器ID从连接池中获取一个连接的方法
	 * 
	 * @param nodeID
	 *            服务器节点的ID
	 * @return 连接对象
	 * @throws ThriftConnectionPoolException
	 *             当获取连接出现错误时抛出该异常
	 */
	public ThriftConnection<T> getConnection(byte[] nodeID) throws ThriftConnectionPoolException;

	/**
	 * 获取一个不阻塞的thrift代理连接
	 * 
	 * @return thrift连接代理对象
	 */
	public ThriftConnection<T> pollConnection();

	/**
	 * 关闭所有连接的方法
	 */
	public void terminateAllConnections();

	/**
	 * 清理连接代理类的方法
	 * 
	 * @param oldHandler
	 *            旧的连接代理类
	 * @param newHandler
	 *            新的连接代理类
	 */
	public void cleanupConnection(ThriftConnectionHandle<T> oldHandler, ThriftConnectionHandle<T> newHandler);

}
