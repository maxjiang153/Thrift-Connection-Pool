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
 * @author jiangwei (ydswcy513@gmail.com)
 * @version V1.0
 */
public interface ThriftConnection<T extends TServiceClient> extends Closeable {

	/**
	 * 获取客户端的方法<br>
	 * 仅在单服务情况下试用 如果在多服务情况下试用则会抛出UnsupportedOperationException异常
	 * 
	 * @return 客户端对象
	 */
	public T getClient();

	/**
	 * 根据服务名称获取客户端的方法<br>
	 * 在多服务情况下试用 如果在单服务情况下试用则直接返回客户端<br>
	 * 如果服务名错误则返回null
	 * 
	 * @param <K>
	 *            thrift客户端类
	 * @param serviceName
	 *            服务名称
	 * @param clazz
	 *            thrift客户端class类
	 * @return 客户端对象
	 */
	public <K extends TServiceClient> K getClient(String serviceName, Class<K> clazz);

	/**
	 * 判断连接是否关闭的表识位
	 * 
	 * @return true为连接已经关闭 false为连接未关闭
	 */
	public boolean isClosed();
	
	/**
	 * 设置该链接的状态的方法<br>
	 * 该状态默认为true 当连接状态设置为false时，该链接不会被连接池回收<br>
	 * 典型的应用案例为当远端服务器处理请求缓慢超时时 thrift报超时异常<br>
	 * 但是连接池本身并不知道连接已经存在问题<br>
	 * 当这个问题连接被回收到连接池被再次获取时<br>
	 * 则会报请求序号不一致的问题<br>
	 * 
	 * @param available 连接的状态
	 */
	public void setAvailable(boolean available);
}
