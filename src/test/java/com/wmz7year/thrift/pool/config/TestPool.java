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

package com.wmz7year.thrift.pool.config;

import java.io.IOException;

import org.apache.thrift.TException;

import com.wmz7year.thrift.pool.ThriftConnectionPool;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.TProtocolType;
import com.wmz7year.thrift.pool.example.Example;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

import junit.framework.TestCase;

public class TestPool extends TestCase {
	public void testPoolApi() {
		ThriftConnectionPoolConfig config = new ThriftConnectionPoolConfig();
		config.setConnectTimeout(3000);
		config.setThriftProtocol(TProtocolType.BINARY);
		config.setClientClass(Example.Client.class);
		config.addThriftServer("127.0.0.1", 9119);
		config.setMaxConnectionPerServer(2);
		config.setMinConnectionPerServer(1);
		config.setLazyInit(false);

		try {
			ThriftConnectionPool<Example.Client> pool = new ThriftConnectionPool<Example.Client>(config);
			Example.Client client = pool.getConnection().getClient();
			client.ping();
		//	pool.close();
		} catch (ThriftConnectionPoolException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}
}
