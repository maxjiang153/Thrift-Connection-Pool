package com.wmz7year.thrift.pool.config;

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

		try {
			ThriftConnectionPool<Example.Client> pool = new ThriftConnectionPool<Example.Client>(config);
			Example.Client client = pool.getConnection();
		} catch (ThriftConnectionPoolException e) {
			e.printStackTrace();
		}
	}
}
