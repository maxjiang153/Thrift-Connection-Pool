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

package com.wmz7year.thrift.pool.connection;

import java.io.Closeable;

import org.apache.thrift.TServiceClient;

/**
 * Thrift连接对象
 * 
 * @Title: ThriftConnection.java
 * @Package com.wmz7year.thrift.pool.connection
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 上午11:08:06
 * @version V1.0
 */
public interface ThriftConnection<T extends TServiceClient> extends Closeable {

	/**
	 * 获取客户端的方法
	 * 
	 * @return 客户端对象
	 */
	public T getClient();

	/**
	 * 判断连接是否关闭的表识位
	 * 
	 * @return true为连接已经关闭 false为连接未关闭
	 */
	public boolean isClosed();
}
