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
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.TProtocolType;
import com.wmz7year.thrift.pool.example.Example;

/*
 * 没有服务器时启动连接连接池的测试
 */
public class NoThriftServerStartPoolTest extends BasicAbstractTest {

	/*
	 * @see com.wmz7year.thrift.pool.BasicAbstractTest#beforeTest()
	 */
	@Override
	protected void beforeTest() throws Exception {

	}

	/*
	 * @see com.wmz7year.thrift.pool.BasicAbstractTest#afterTest()
	 */
	@Override
	protected void afterTest() throws Exception {

	}

	public void testNoThriftServerStartPool() throws Exception {
		ThriftConnectionPoolConfig config = new ThriftConnectionPoolConfig();
		config.setConnectTimeout(3000);
		config.setThriftProtocol(TProtocolType.BINARY);
		config.setClientClass(Example.Client.class);
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
		// 设置支持没有thrift服务器启动
		config.setNoServerStartUp(true);

		ThriftConnectionPool<Example.Client> pool = new ThriftConnectionPool<Example.Client>(config);
		assertEquals(0, pool.getThriftServerCount());
		pool.close();
	}
}
