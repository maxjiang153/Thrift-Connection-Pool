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
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.TProtocolType;
import com.wmz7year.thrift.pool.example.Example;

/*
 * 当连接池从服务器获取N次连接后依然无法获取连接时  应当删除服务器信息
 */
public class GetConnectionFromServerFailedCounterTest extends BasicAbstractTest {
	private List<ThriftServerInfo> servers;

	@Override
	protected void beforeTest() throws Exception {
		servers = startServers(1);
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
		config.addThriftServer(servers.get(0));
		config.setMaxConnectionPerServer(2);
		config.setMinConnectionPerServer(1);
		config.setIdleMaxAge(2, TimeUnit.SECONDS);
		config.setMaxConnectionAge(2);
		config.setLazyInit(false);
		config.setAcquireIncrement(2);
		config.setAcquireRetryDelay(2000);

		config.setAcquireRetryAttempts(1);
		config.setMaxConnectionCreateFailedCount(1);
		config.setConnectionTimeoutInMs(5000);

		ThriftConnectionPool<Example.Client> pool = new ThriftConnectionPool<Example.Client>(config);

		// 正常获取连接
		pool.getConnection().getClient().ping();

		// 关闭服务器
		stopAllServers();
		// 等待连接关闭
		TimeUnit.SECONDS.sleep(5);

		try {
			// 服务器失效的情况下获取连接
			pool.getConnection().getClient().ping();
		} catch (Exception e) {
			// ignore
		}
		
		// 移除后服务器数量应该为0
		assertEquals(pool.getThriftServerCount(), 0);
		
		try {
			// 再次获取连接应该抛出无可用服务器的异常
			pool.getConnection().getClient().ping();
		} catch (Exception e) {
			e.printStackTrace();
		}

	
		pool.close();
	}

}
