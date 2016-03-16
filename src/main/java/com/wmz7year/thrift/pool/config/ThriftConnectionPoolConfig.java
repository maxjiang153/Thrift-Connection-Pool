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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/**
 * thrift连接池配置类<br>
 * 
 * @author jiangwei (ydswcy513@gmail.com)
 * @version V1.0
 */
public class ThriftConnectionPoolConfig {
	private static final Logger logger = LoggerFactory.getLogger(ThriftConnectionPoolConfig.class);
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
	 * 多服务情况下的客户端列表
	 */
	private Map<String, Class<? extends TServiceClient>> clientClasses = new HashMap<>();

	/**
	 * 配置的服务器列表
	 */
	private Collection<ThriftServerInfo> thriftServers = new HashSet<>();

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
	 * 未使用连接关闭时间
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

	/**
	 * 最大连续获取失败连接的次数<br>
	 * 如果到了该值依然无法获取连接 连接池则会排出对应问题连接的服务器
	 */
	private int maxConnectionCreateFailedCount = 3;

	/**
	 * 是否支持没有服务器时启动连接池<br>
	 * 在这种情况下无法获取连接 需要等待有服务器时才可以获取连接
	 */
	private boolean noServerStartUp;

	/**
	 * thrift接口模式
	 */
	private ThriftServiceType thriftServiceType;

	public ThriftConnectionPoolConfig() {
		this(ThriftServiceType.SINGLE_INTERFACE);
	}

	public ThriftConnectionPoolConfig(ThriftServiceType thriftServiceType) {
		this.thriftServiceType = thriftServiceType;
	}

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
		addThriftServer(host, port, null);
	}

	/**
	 * 添加thrift服务器信息的方法
	 * 
	 * @param host
	 *            服务器地址
	 * @param port
	 *            服务器端口
	 * @param serverID
	 *            服务器ID
	 */
	public void addThriftServer(String host, int port, byte[] serverID) {
		addThriftServer(new ThriftServerInfo(host, port, serverID));
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
		List<ThriftServerInfo> servers = new ArrayList<>();
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

	public int getMaxConnectionCreateFailedCount() {
		return maxConnectionCreateFailedCount;
	}

	public void setMaxConnectionCreateFailedCount(int maxConnectionCreateFailedCount) {
		this.maxConnectionCreateFailedCount = maxConnectionCreateFailedCount;
	}

	public void addThriftClientClass(String serviceName, Class<? extends TServiceClient> clazz) {
		this.clientClasses.put(serviceName, clazz);
	}

	public Map<String, Class<? extends TServiceClient>> getThriftClientClasses() {
		return this.clientClasses;
	}

	public ThriftServiceType getThriftServiceType() {
		return thriftServiceType;
	}

	public boolean isNoServerStartUp() {
		return noServerStartUp;
	}

	public void setNoServerStartUp(boolean noServerStartUp) {
		this.noServerStartUp = noServerStartUp;
	}

	/**
	 * 检查配置信息的方法
	 * 
	 * @throws ThriftConnectionPoolException
	 *             当配置信息出现问题时抛出该异常
	 */
	public void check() throws ThriftConnectionPoolException {
		if (connectTimeout <= 0) {
			throw new ThriftConnectionPoolException("连接超时时间必须大于0");
		}

		// 判断是否是单接口模式 如果是则只检测单接口
		if (getThriftServiceType() == ThriftServiceType.SINGLE_INTERFACE) {
			if (clientClass == null) {
				throw new ThriftConnectionPoolException("thrift客户端实现类未设置");
			}
			// 检测ping方法
			try {
				clientClass.getMethod("ping");
			} catch (NoSuchMethodException e) {
				throw new ThriftConnectionPoolException("Thrift客户端实现类必须带有ping()方法用于检测连接");
			}
		} else if (getThriftServiceType() == ThriftServiceType.MULTIPLEXED_INTERFACE) {
			if (clientClasses.size() == 0) {
				throw new ThriftConnectionPoolException("多服务thrift客户端实现类未设置");
			}
			// 检测所有接口
			List<String> toRemoveClasses = new ArrayList<>();
			Iterator<Entry<String, Class<? extends TServiceClient>>> iterator = clientClasses.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Class<? extends TServiceClient>> entry = iterator.next();
				Class<? extends TServiceClient> clazz = entry.getValue();
				try {
					clazz.getMethod("ping");
				} catch (NoSuchMethodException e) {
					toRemoveClasses.add(entry.getKey());
					logger.warn("接口：" + entry.getKey() + " 没有实现ping方法 无法创建对应的服务客户端");
				}
			}
			for (String toRemoveClass : toRemoveClasses) {
				clientClasses.remove(toRemoveClass);
			}
			Iterator<String> servicesNameIterator = clientClasses.keySet().iterator();
			while (servicesNameIterator.hasNext()) {
				logger.info("注册服务客户端：" + servicesNameIterator.next());
			}
		}

		if (maxConnectionPerServer <= 0) {
			throw new ThriftConnectionPoolException("每台服务器的最大连接数必须大于0");
		}

		if (minConnectionPerServer < 0) {
			throw new ThriftConnectionPoolException("每台服务器最小连接数不能小于0");
		}

		if (minConnectionPerServer > maxConnectionPerServer) {
			throw new ThriftConnectionPoolException("每台服务器的最小连接数不能超过最大连接数配置");
		}

		if (idleConnectionTestPeriodInSeconds <= 0) {
			throw new ThriftConnectionPoolException("检测时间周期必须大于0秒");
		}

		if (idleMaxAgeInSeconds <= 0) {
			throw new ThriftConnectionPoolException("未使用连接关闭时间必须大于0秒");
		}

		if (maxConnectionAgeInSeconds <= 0) {
			throw new ThriftConnectionPoolException("连接最大生存时间必须大于0秒");
		}

		if (acquireRetryAttempts < 0) {
			throw new ThriftConnectionPoolException("获取连接重试次数不能小于0");
		}

		if (acquireRetryDelayInMs < 0) {
			throw new ThriftConnectionPoolException("获取连接重试等待时间不能小于0毫秒");
		}

		if (connectionTimeoutInMs < 0) {
			throw new ThriftConnectionPoolException("获取连接等待时间不能小于0毫秒");
		}

		if (acquireIncrement <= 0) {
			throw new ThriftConnectionPoolException("每次创建原始连接的数量必须大于0个");
		}
	}

	/**
	 * thrift 通讯管道类型
	 * 
	 * @author jiangwei (ydswcy513@gmail.com)
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
	 * @author jiangwei (ydswcy513@gmail.com)
	 * @version V1.0
	 */
	public enum ServiceOrder {
		FIFO, LIFO
	}

	/**
	 * thrift服务类型<br>
	 * 单接口模式还是多接口模式
	 * 
	 * @author jiangwei (ydswcy513@gmail.com)
	 * @version V1.0
	 */
	public enum ThriftServiceType {
		/**
		 * 单接口模式
		 */
		SINGLE_INTERFACE,
		/**
		 * 多接口模式
		 */
		MULTIPLEXED_INTERFACE
	}

}
