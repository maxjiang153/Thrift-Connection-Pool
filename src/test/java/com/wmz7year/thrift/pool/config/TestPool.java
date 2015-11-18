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
		config.addThriftServer("127.0.0.1", 9999);

		try {
			ThriftConnectionPool<Example.Client> pool = new ThriftConnectionPool<Example.Client>(config);
			Example.Client client = pool.getConnection();
			client.ping();
			pool.close();
		} catch (ThriftConnectionPoolException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
