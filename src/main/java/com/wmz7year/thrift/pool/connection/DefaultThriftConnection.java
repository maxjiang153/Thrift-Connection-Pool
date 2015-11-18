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

import java.io.IOException;

import org.apache.thrift.TServiceClient;

/**
 * 默认实现的thrift客户端对象
 * 
 * @Title: DefaultThriftConnection.java
 * @Package com.wmz7year.thrift.pool.connection
 * @author jiangwei (jiangwei@1318.com)
 * @date 2015年11月18日 下午1:37:59
 * @version V1.0
 */
public class DefaultThriftConnection<T extends TServiceClient> implements ThriftConnection<T> {

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public T getClient() {
		// TODO Auto-generated method stub
		return null;
	}

}
