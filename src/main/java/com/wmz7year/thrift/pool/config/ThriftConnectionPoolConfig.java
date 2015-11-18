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

	/**
	 * 每批连接创建的数量
	 */
	private int acquireIncrement = 2;

	public TProtocolType getThriftProtocol() {
		return thriftProtocol;
	}

	public void setThriftProtocol(TProtocolType thriftProtocol) {
		this.thriftProtocol = thriftProtocol;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Class<? extends TServiceClient> getClientClass() {
		return clientClass;
	}

	public void setClientClass(Class<? extends TServiceClient> clientClass) {
		this.clientClass = clientClass;
	}

	/**
	 * 添加thrift服务器信息的方法
	 * 
	 * @param host
	 *            服务器地址
	 * @param port
	 *            服务器端口
	 */
	public void addThriftServer(String host, int port) {
		addThriftServer(new ThriftServerInfo(host, port));
	}

	/**
	 * 添加thrift服务信息对象的方法
	 * 
	 * @param serverInfo
	 *            服务器信息对象
	 */
	public void addThriftServer(ThriftServerInfo serverInfo) {
		thriftServers.add(serverInfo);
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

	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public int getMaxConnectionPerServer() {
		return maxConnectionPerServer;
	}

	public void setMaxConnectionPerServer(int maxConnectionPerServer) {
		this.maxConnectionPerServer = maxConnectionPerServer;
	}

	public int getMinConnectionPerServer() {
		return minConnectionPerServer;
	}

	public void setMinConnectionPerServer(int minConnectionPerServer) {
		this.minConnectionPerServer = minConnectionPerServer;
	}

	public void setIdleConnectionTestPeriodInSeconds(long idleConnectionTestPeriod, TimeUnit timeUnit) {
		this.idleConnectionTestPeriodInSeconds = TimeUnit.SECONDS.convert(idleConnectionTestPeriod,
				checkNotNull(timeUnit));
	}

	public long getIdleConnectionTestPeriod(TimeUnit timeUnit) {
		return timeUnit.convert(this.idleConnectionTestPeriodInSeconds, TimeUnit.SECONDS);
	}

	public void setIdleMaxAgeInSeconds(long idleMaxAge) {
		setIdleMaxAge(idleMaxAge, TimeUnit.SECONDS);
	}

	public void setIdleMaxAge(long idleMaxAge, TimeUnit timeUnit) {
		this.idleMaxAgeInSeconds = TimeUnit.SECONDS.convert(idleMaxAge, checkNotNull(timeUnit));
	}

	public long getIdleMaxAge(TimeUnit timeUnit) {
		return timeUnit.convert(this.idleMaxAgeInSeconds, TimeUnit.SECONDS);
	}

	public void setMaxConnectionAge(long maxConnectionAgeInSeconds) {
		this.maxConnectionAgeInSeconds = maxConnectionAgeInSeconds;
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

	public void setServiceOrder(ServiceOrder serviceOrder) {
		this.serviceOrder = serviceOrder;
	}

	public int getAcquireRetryAttempts() {
		return this.acquireRetryAttempts;
	}

	public void setAcquireRetryAttempts(int acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
	}

	public long getAcquireRetryDelayInMs() {
		return this.acquireRetryDelayInMs;
	}

	public void setAcquireRetryDelay(int acquireRetryDelayInMs) {
		this.acquireRetryDelayInMs = acquireRetryDelayInMs;
	}

	/**
	 * 获取当分区连接小于x%的时候触发创建连接信号量
	 * 
	 * @return 最小分区空闲连接比例
	 */
	public int getPoolAvailabilityThreshold() {
		return this.poolAvailabilityThreshold;
	}

	public void setPoolAvailabilityThreshold(int poolAvailabilityThreshold) {
		this.poolAvailabilityThreshold = poolAvailabilityThreshold;
	}

	public long getConnectionTimeoutInMs() {
		return this.connectionTimeoutInMs;
	}

	public void setConnectionTimeoutInMs(long connectionTimeoutinMs) {
		setConnectionTimeout(connectionTimeoutinMs, TimeUnit.MILLISECONDS);
	}

	public void setConnectionTimeout(long connectionTimeout, TimeUnit timeUnit) {
		this.connectionTimeoutInMs = TimeUnit.MILLISECONDS.convert(connectionTimeout, timeUnit);
	}

	public int getAcquireIncrement() {
		return acquireIncrement;
	}

	public void setAcquireIncrement(int acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
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
