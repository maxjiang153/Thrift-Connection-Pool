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

import java.util.Collection;
import java.util.HashSet;

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
	 * 连接池名称
	 */
	private String poolName;

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

	/**
	 * 配置的服务器列表
	 */
	private Collection<ThriftServerInfo> thriftServers = new HashSet<ThriftServerInfo>();

	/**
	 * 是否是懒加载连接
	 */
	private boolean lazyInit;

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
	 * 添加thrift服务器信息的方法
	 * 
	 * @param host
	 *            服务器地址
	 * @param port
	 *            服务器端口
	 */
	public ThriftConnectionPoolConfig addThriftServer(String host, int port) {
		addThriftServer(new ThriftServerInfo(host, port));
		return this;
	}

	/**
	 * 添加thrift服务信息对象的方法
	 * 
	 * @param serverInfo
	 *            服务器信息对象
	 */
	public ThriftConnectionPoolConfig addThriftServer(ThriftServerInfo serverInfo) {
		thriftServers.add(serverInfo);
		return this;
	}

	/**
	 * 获取配置的thrift服务器列表的方法
	 * 
	 * @return thrift服务器列表集合
	 */
	public Collection<ThriftServerInfo> getThriftServers() {
		return thriftServers;
	}

	public boolean isLazyInit() {
		return lazyInit;
	}

	public ThriftConnectionPoolConfig setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
		return this;
	}

	public String getPoolName() {
		return poolName;
	}

	public ThriftConnectionPoolConfig setPoolName(String poolName) {
		this.poolName = poolName;
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
