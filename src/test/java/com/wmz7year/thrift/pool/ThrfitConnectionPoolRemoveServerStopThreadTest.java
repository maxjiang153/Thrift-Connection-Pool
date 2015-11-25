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

import org.apache.thrift.TServiceClient;

import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig;
import com.wmz7year.thrift.pool.config.ThriftServerInfo;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.TProtocolType;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.ThriftServiceType;
import com.wmz7year.thrift.pool.example.Example;
import com.wmz7year.thrift.pool.example.Other;

/*
 * 当服务器移除连接池时 需要及时停止对应的监控线程
 */
public class ThrfitConnectionPoolRemoveServerStopThreadTest extends BasicAbstractTest {

	private List<ThriftServerInfo> servers;

	/*
	 * @see com.wmz7year.thrift.pool.BasicAbstractTest#beforeTest()
	 */
	@Override
	protected void beforeTest() throws Exception {
		this.servers = startServers(1);
	}

	/*
	 * @see com.wmz7year.thrift.pool.BasicAbstractTest#afterTest()
	 */
	@Override
	protected void afterTest() throws Exception {
		// ignore
	}

	public void testThriftConnectionPoolRemoveServerStopThread() throws Exception {

		ThriftConnectionPoolConfig config = new ThriftConnectionPoolConfig(ThriftServiceType.MULTIPLEXED_INTERFACE);
		config.setConnectTimeout(3000);
		config.setThriftProtocol(TProtocolType.BINARY);
		// 该端口不存在
		ThriftServerInfo thriftServerInfo = servers.get(0);
		config.addThriftServer(thriftServerInfo);
		config.addThriftClientClass("example", Example.Client.class);
		config.addThriftClientClass("other", Other.Client.class);

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

		ThriftConnectionPool<TServiceClient> pool = new ThriftConnectionPool<TServiceClient>(config);
		pool.getConnection().close();

		// 移除并且停止服务
		pool.removeThriftServer(thriftServerInfo);
		stopAllServers();
		pool.close();

	}

}
