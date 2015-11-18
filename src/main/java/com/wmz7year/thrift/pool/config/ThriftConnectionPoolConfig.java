package com.wmz7year.thrift.pool.config;

import org.apache.thrift.TServiceClient;

/**
 * thrift连接池配置类<br>
 * 
 * @Title: ThriftConnectionPoolConfig.java
 * @Package com.wmz7year.thrift.pool.config
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 上午9:43:35
 * @version V1.0
 */
public class ThriftConnectionPoolConfig {

	/**
	 * 连接通讯管道类型
	 */
	private TProtocolType thriftProtocol;

	/**
	 * 连接超时时间
	 */
	private int connectTimeout;

	/**
	 * thrift客户端类对象
	 */
	private Class<? extends TServiceClient> clientClass;

	public TProtocolType getThriftProtocol() {
		return thriftProtocol;
	}

	public ThriftConnectionPoolConfig setThriftProtocol(TProtocolType thriftProtocol) {
		this.thriftProtocol = thriftProtocol;
		return this;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public ThriftConnectionPoolConfig setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public Class<? extends TServiceClient> getClientClass() {
		return clientClass;
	}

	public ThriftConnectionPoolConfig setClientClass(Class<? extends TServiceClient> clientClass) {
		this.clientClass = clientClass;
		return this;
	}

	/**
	 * thrift 通讯管道类型
	 * 
	 * @Title: ThriftConnectionPoolConfig.java
	 * @Package com.wmz7year.thrift.pool.config
	 * @author jiangwei (ydswcy513@gmail.com)
	 * @date 2015年11月18日 上午10:03:00
	 * @version V1.0
	 */
	public enum TProtocolType {
		/**
		 * 二进制通讯管道类型
		 */
		BINARY,
		/**
		 * JSON通讯管道类型
		 */
		JSON
	}

}
