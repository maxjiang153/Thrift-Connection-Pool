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

/**
 * thrift连接最大时间检测线程
 * 
 * @author jiangwei (ydswcy513@gmail.com)
 * @version V1.0
 */
public class ThriftConnectionMaxAgeThread<T extends TServiceClient> extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(ThriftConnectionMaxAgeThread.class);

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
	/**
	 * 最大生存时间
	 */
	private long maxConnectionAge;

	public ThriftConnectionMaxAgeThread(ThriftConnectionPartition<T> thriftConnectionPartition,
			ThriftConnectionPool<T> thriftConnectionPool, long maxConnectionAge, ServiceOrder lifoMode) {
		this.thriftConnectionPartition = thriftConnectionPartition;
		this.maxConnectionAge = maxConnectionAge;
		this.lifoMode = lifoMode;
		this.thriftConnectionPool = thriftConnectionPool;
		this.thriftConnectionPartition.registConnectionMaxAgeThread(this);
	}

	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		ThriftConnectionHandle<T> connection;
		long tmp;
		long nextCheckInMs = this.maxConnectionAge;

		int partitionSize = this.thriftConnectionPartition.getAvailableConnections();
		long currentTime = System.currentTimeMillis();
		for (int i = 0; i < partitionSize; i++) {
			try {
				connection = this.thriftConnectionPartition.poolFreeConnection();

				if (connection != null) {
					connection.setOriginatingPartition(this.thriftConnectionPartition);

					tmp = this.maxConnectionAge - (currentTime - connection.getConnectionCreationTimeInMs());

					if (tmp < nextCheckInMs) {
						nextCheckInMs = tmp;
					}

					// 判断连接是否过期
					if (connection.isExpired(currentTime)) {
						// 关闭连接
						closeConnection(connection);
						continue;
					}

					if (lifoMode == ServiceOrder.LIFO) {
						if (!(connection.getConnectionPartition().getFreeConnections().offer(connection))) {
							connection.internalClose();
						}
					} else {
						this.thriftConnectionPool.putConnectionBackInPartition(connection);
					}
					// 避免cpu使用率过高 进行一段时间休眠
					Thread.sleep(20L);
				}
			} catch (Throwable e) {
				logger.error("Connection max age thread exception.", e);
			}
		}

	}

	/**
	 * 关闭thrift超时连接的方法
	 * 
	 * @param connection
	 *            需要关闭的超时连接
	 */
	protected void closeConnection(ThriftConnectionHandle<T> connection) {
		if (connection != null) {
			try {
				connection.internalClose();
			} catch (Throwable t) {
				logger.error("Destroy connection exception", t);
			} finally {
				this.thriftConnectionPool.postDestroyConnection(connection);
			}
		}
	}

}
