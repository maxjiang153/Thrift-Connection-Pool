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

import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wmz7year.thrift.pool.connection.ThriftConnection;

/**
 * thrift连接代理类
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

	public ThriftConnectionHandle(ThriftConnection<T> thriftConnection,
			ThriftConnectionPartition<T> thriftConnectionPartition, ThriftConnectionPool<T> thriftConnectionPool,
			boolean recreating) {
		// TODO Auto-generated constructor stub
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

}
