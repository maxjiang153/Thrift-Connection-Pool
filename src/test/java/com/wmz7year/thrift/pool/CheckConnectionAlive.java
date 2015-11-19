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

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig;
import com.wmz7year.thrift.pool.config.ThriftServerInfo;
import com.wmz7year.thrift.pool.connection.ThriftConnection;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.TProtocolType;
import com.wmz7year.thrift.pool.example.Example;
import com.wmz7year.thrift.pool.example.Example.Client;

/*
 * 检测连接是否可用  反射调用ping方法
 */
public class CheckConnectionAlive extends BasicAbstractTest {

	private List<ThriftServerInfo> servers;

	@Override
	protected void beforeTest() throws Exception {
		this.servers = startServers(1);
	}

	@Override
	protected void afterTest() throws Exception {
	}

	public void testConnectionAlive() throws Exception {
		ThriftConnectionPoolConfig config = new ThriftConnectionPoolConfig();
		config.setConnectTimeout(3000);
		config.setThriftProtocol(TProtocolType.BINARY);
		config.setClientClass(Example.Client.class);
		for (ThriftServerInfo thriftServerInfo : servers) {
			config.addThriftServer(thriftServerInfo.getHost(), thriftServerInfo.getPort());
		}
		config.setMaxConnectionPerServer(2);
		config.setMinConnectionPerServer(1);
		config.setIdleMaxAge(2, TimeUnit.SECONDS);
		config.setMaxConnectionAge(2);
		config.setLazyInit(false);
		ThriftConnectionPool<Example.Client> pool = new ThriftConnectionPool<Example.Client>(config);

		ThriftConnection<Client> connection = pool.getConnection();
		assertTrue(pool.isConnectionHandleAlive((ThriftConnectionHandle<Client>) connection));

		pool.close();
	}

}
