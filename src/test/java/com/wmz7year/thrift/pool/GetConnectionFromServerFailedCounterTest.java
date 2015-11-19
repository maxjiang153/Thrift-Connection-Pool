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

import java.util.concurrent.TimeUnit;

import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig;
import com.wmz7year.thrift.pool.config.ThriftServerInfo;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.TProtocolType;
import com.wmz7year.thrift.pool.example.Example;

/*
 * 当连接池从服务器获取N次连接后依然无法获取连接时  应当删除服务器信息
 */
public class GetConnectionFromServerFailedCounterTest extends BasicAbstractTest {

	@Override
	protected void beforeTest() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void afterTest() throws Exception {
		// TODO Auto-generated method stub

	}

	public void testConnectionFaildCounter() throws Exception {
		ThriftConnectionPoolConfig config = new ThriftConnectionPoolConfig();
		config.setConnectTimeout(3000);
		config.setThriftProtocol(TProtocolType.BINARY);
		config.setClientClass(Example.Client.class);
		// 该端口不存在
		config.addThriftServer("127.0.0.1", 39999);
		config.setMaxConnectionPerServer(2);
		config.setMinConnectionPerServer(1);
		config.setIdleMaxAge(2, TimeUnit.SECONDS);
		config.setMaxConnectionAge(2);
		config.setLazyInit(false);

		config.setMaxConnectionCreateFailedCount(3);

		ThriftConnectionPool<Example.Client> pool = new ThriftConnectionPool<Example.Client>(config);

		// 获取连接
		pool.getConnection();

		pool.close();
	}

}
