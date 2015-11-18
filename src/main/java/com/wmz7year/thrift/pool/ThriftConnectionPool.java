package com.wmz7year.thrift.pool;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;

import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/**
 * thrift连接池主类
 * 
 * @Title: ThriftConnectionPool.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 上午9:54:53
 * @version V1.0
 */
public class ThriftConnectionPool<T extends TServiceClient> implements Serializable, Closeable {
	private static final Logger logger = LoggerFactory.getLogger(ThriftConnectionPool.class);
	private static final long serialVersionUID = 6524222103868846620L;

	/**
	 * 连接池配置对象
	 */
	private ThriftConnectionPoolConfig config;

	/**
	 * 构造器
	 * 
	 * @param config
	 *            连接池配置对象
	 * @throws ThriftConnectionPoolException
	 *             当发生错误的时候抛出该异常信息
	 */
	public ThriftConnectionPool(ThriftConnectionPoolConfig config) throws ThriftConnectionPoolException {
		this.config = config;
	}

	/*
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * 从连接池中获取一个连接的方法
	 * 
	 * @return 连接对象
	 * @throws ThriftConnectionPoolException
	 *             当获取连接出现错误时抛出该异常
	 */
	public T getConnection() throws ThriftConnectionPoolException {
		// TODO Auto-generated method stub
		return null;
	}

}
