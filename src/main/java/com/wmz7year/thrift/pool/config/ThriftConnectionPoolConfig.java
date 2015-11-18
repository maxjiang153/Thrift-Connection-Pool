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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

	/**
	 * 每台服务器最大的连接数
	 */
	private int maxConnectionPerServer = 0;

	/**
	 * 每台服务器最小连接数
	 */
	private int minConnectionPerServer = 0;

	/**
	 * 连接检测时间周期
	 */
	private long idleConnectionTestPeriodInSeconds = 10;

	/**
	 * 为使用连接关闭时间
	 */
	private long idleMaxAgeInSeconds = 20;

	/**
	 * 连接最大存活时间
	 */
	private long maxConnectionAgeInSeconds = 0;

	/**
	 * 当连接获取失败时 重试获取的次数
	 */
	private int acquireRetryAttempts = 5;

	/**
	 * 当连接获取失败时 重试获取的时间间隔
	 */
	private long acquireRetryDelayInMs = 7000;

	/**
	 * 队列模式
	 */
	private ServiceOrder serviceOrder = ServiceOrder.FIFO;
	/**
	 * 当需要创建连接时检查的分区连接空闲连接比例
	 */
	private int poolAvailabilityThreshold = 0;
	/**
	 * 连接获取的超时时间
	 */
	private long connectionTimeoutInMs = 0;

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
	public List<ThriftServerInfo> getThriftServers() {
		List<ThriftServerInfo> servers = new ArrayList<ThriftServerInfo>();
		servers.addAll(thriftServers);
		return servers;
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

	public int getMaxConnectionPerServer() {
		return maxConnectionPerServer;
	}

	public ThriftConnectionPoolConfig setMaxConnectionPerServer(int maxConnectionPerServer) {
		this.maxConnectionPerServer = maxConnectionPerServer;
		return this;
	}

	public int getMinConnectionPerServer() {
		return minConnectionPerServer;
	}

	public ThriftConnectionPoolConfig setMinConnectionPerServer(int minConnectionPerServer) {
		this.minConnectionPerServer = minConnectionPerServer;
		return this;
	}

	public ThriftConnectionPoolConfig setIdleConnectionTestPeriodInSeconds(long idleConnectionTestPeriod,
			TimeUnit timeUnit) {
		this.idleConnectionTestPeriodInSeconds = TimeUnit.SECONDS.convert(idleConnectionTestPeriod,
				checkNotNull(timeUnit));
		return this;
	}

	public long getIdleConnectionTestPeriod(TimeUnit timeUnit) {
		return timeUnit.convert(this.idleConnectionTestPeriodInSeconds, TimeUnit.SECONDS);
	}

	public ThriftConnectionPoolConfig setIdleMaxAgeInSeconds(long idleMaxAge) {
		return setIdleMaxAge(idleMaxAge, TimeUnit.SECONDS);
	}

	public ThriftConnectionPoolConfig setIdleMaxAge(long idleMaxAge, TimeUnit timeUnit) {
		this.idleMaxAgeInSeconds = TimeUnit.SECONDS.convert(idleMaxAge, checkNotNull(timeUnit));
		return this;
	}

	public long getIdleMaxAge(TimeUnit timeUnit) {
		return timeUnit.convert(this.idleMaxAgeInSeconds, TimeUnit.SECONDS);
	}

	public ThriftConnectionPoolConfig setMaxConnectionAge(long maxConnectionAgeInSeconds) {
		this.maxConnectionAgeInSeconds = maxConnectionAgeInSeconds;
		return this;
	}

	public long getMaxConnectionAge(TimeUnit timeUnit) {
		return timeUnit.convert(this.maxConnectionAgeInSeconds, TimeUnit.SECONDS);
	}

	public long getMaxConnectionAgeInSeconds() {
		return this.maxConnectionAgeInSeconds;
	}

	public ServiceOrder getServiceOrder() {
		return serviceOrder;
	}

	public ThriftConnectionPoolConfig setServiceOrder(ServiceOrder serviceOrder) {
		this.serviceOrder = serviceOrder;
		return this;
	}

	public int getAcquireRetryAttempts() {
		return this.acquireRetryAttempts;
	}

	public ThriftConnectionPoolConfig setAcquireRetryAttempts(int acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
		return this;
	}

	public long getAcquireRetryDelayInMs() {
		return this.acquireRetryDelayInMs;
	}

	public ThriftConnectionPoolConfig setAcquireRetryDelay(int acquireRetryDelayInMs) {
		this.acquireRetryDelayInMs = acquireRetryDelayInMs;
		return this;
	}

	/**
	 * 获取当分区连接小于x%的时候触发创建连接信号量
	 * 
	 * @return 最小分区空闲连接比例
	 */
	public int getPoolAvailabilityThreshold() {
		return this.poolAvailabilityThreshold;
	}

	public ThriftConnectionPoolConfig setPoolAvailabilityThreshold(int poolAvailabilityThreshold) {
		this.poolAvailabilityThreshold = poolAvailabilityThreshold;
		return this;
	}

	public long getConnectionTimeoutInMs() {
		return this.connectionTimeoutInMs;
	}

	public ThriftConnectionPoolConfig setConnectionTimeoutInMs(long connectionTimeoutinMs) {
		return setConnectionTimeout(connectionTimeoutinMs, TimeUnit.MILLISECONDS);
	}

	public ThriftConnectionPoolConfig setConnectionTimeout(long connectionTimeout, TimeUnit timeUnit) {
		this.connectionTimeoutInMs = TimeUnit.MILLISECONDS.convert(connectionTimeout, timeUnit);
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

	/**
	 * 队列模式<br>
	 * 先进先出还是先进后出
	 * 
	 * @Title: ThriftConnectionPoolConfig.java
	 * @Package com.wmz7year.thrift.pool.config
	 * @author jiangwei (ydswcy513@gmail.com)
	 * @date 2015年11月18日 下午1:20:47
	 * @version V1.0
	 */
	public enum ServiceOrder {
		FIFO, LIFO
	}

}
