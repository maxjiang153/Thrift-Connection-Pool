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

import java.util.TimerTask;

import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.ServiceOrder;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/**
 * 连接测试线程<br>
 * 用于测试分区中的连接可用性
 * 
 * @Title: ThriftConnectionTesterThread.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 下午1:17:20
 * @version V1.0
 */
public class ThriftConnectionTesterThread<T extends TServiceClient> extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(ThriftConnectionTesterThread.class);
	/**
	 * 当连接闲置多长时间后进行连接测试
	 */
	private long idleConnectionTestPeriodInMs;
	/**
	 * 连接的最大闲置时间 超过该时间会关闭连接
	 */
	private long idleMaxAgeInMs;
	/**
	 * 连接分区对象
	 */
	private ThriftConnectionPartition<T> thriftConnectionPartition;
	/**
	 * 连接池对象
	 */
	private ThriftConnectionPool<T> thriftConnectionPool;
	/**
	 * lifo模式
	 */
	private ServiceOrder lifoMode;

	public ThriftConnectionTesterThread(ThriftConnectionPartition<T> thriftConnectionPartition,
			ThriftConnectionPool<T> thriftConnectionPool, long idleMaxAge, long idleConnectionTestPeriod,
			ServiceOrder lifoMode) {
		this.thriftConnectionPartition = thriftConnectionPartition;
		this.idleMaxAgeInMs = idleMaxAge;
		this.idleConnectionTestPeriodInMs = idleConnectionTestPeriod;
		this.thriftConnectionPool = thriftConnectionPool;
		this.lifoMode = lifoMode;
		this.thriftConnectionPartition.registConnectionTesterThread(this);
	}

	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		ThriftConnectionHandle<T> connection = null;
		long tmp;
		try {
			long nextCheckInMs = this.idleConnectionTestPeriodInMs;
			if (this.idleMaxAgeInMs > 0) {
				if (this.idleConnectionTestPeriodInMs == 0) {
					nextCheckInMs = this.idleMaxAgeInMs;
				} else {
					nextCheckInMs = Math.min(nextCheckInMs, this.idleMaxAgeInMs);
				}
			}

			int partitionSize = this.thriftConnectionPartition.getAvailableConnections();
			long currentTimeInMs = System.currentTimeMillis();
			for (int i = 0; i < partitionSize; i++) {
				// 从分区中获取一个连接进行测试
				connection = this.thriftConnectionPartition.getFreeConnections().poll();
				if (connection != null) {
					connection.setOriginatingPartition(this.thriftConnectionPartition);

					// 检查连接是否出现问题或者过期
					if (connection.isPossiblyBroken() || ((this.idleMaxAgeInMs > 0) && (System.currentTimeMillis()
							- connection.getConnectionLastUsedInMs() > this.idleMaxAgeInMs))) {
						closeConnection(connection);
						continue;
					}

					// 检测连接是否可用以及是否到了测试时间
					if (this.idleConnectionTestPeriodInMs > 0
							&& (currentTimeInMs
									- connection.getConnectionLastUsedInMs() > this.idleConnectionTestPeriodInMs)
							&& (currentTimeInMs
									- connection.getConnectionLastResetInMs() >= this.idleConnectionTestPeriodInMs)) {
						// 检测连接是否可用
						if (!this.thriftConnectionPool.isConnectionHandleAlive(connection)) {
							closeConnection(connection);
							continue;
						}

						tmp = this.idleConnectionTestPeriodInMs;
						if (this.idleMaxAgeInMs > 0) {
							tmp = Math.min(tmp, this.idleMaxAgeInMs);
						}
					} else {
						tmp = Math.abs(this.idleConnectionTestPeriodInMs
								- (currentTimeInMs - connection.getConnectionLastResetInMs()));
						long tmp2 = Math
								.abs(this.idleMaxAgeInMs - (currentTimeInMs - connection.getConnectionLastUsedInMs()));
						if (this.idleMaxAgeInMs > 0) {
							tmp = Math.min(tmp, tmp2);
						}

					}
					if (tmp < nextCheckInMs) {
						nextCheckInMs = tmp;
					}

					if (lifoMode == ServiceOrder.LIFO) {
						// 如果不能正确的把链接还给分区 则关闭
						if (!(connection.getConnectionPartition().getFreeConnections().offer(connection))) {
							connection.internalClose();
						}
					} else {
						this.thriftConnectionPool.putConnectionBackInPartition(connection);
					}

					// 避免cpu使用率过高 进行一段时间休眠
					Thread.sleep(20L);
				}

			}

		} catch (Throwable e) {
			logger.error("Connection tester thread interrupted", e);
		}

	}

	/**
	 * 关闭连接的方法
	 * 
	 * @param connection
	 *            需要关闭的连接对象
	 */
	protected synchronized void closeConnection(ThriftConnectionHandle<T> connection) {

		if (connection != null && !connection.isClosed()) {
			try {
				connection.internalClose();
			} catch (ThriftConnectionPoolException e) {
				logger.error("Destroy connection exception", e);
			} finally {
				this.thriftConnectionPool.postDestroyConnection(connection);
				connection.getConnectionPartition().getPoolWatchThreadSignalQueue().offer(new Object());
			}
		}
	}

}
