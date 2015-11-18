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

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.thrift.TServiceClient;

import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig;
import com.wmz7year.thrift.pool.config.ThriftServerInfo;
import com.wmz7year.thrift.pool.connection.ThriftConnection;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/**
 * 连接分区实体类<br>
 * 每台服务器都意味着一个连接分区<br>
 * 不同服务器之间的分区是各自独立的
 * 
 * @Title: ThriftConnectionPartition.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 上午11:35:07
 * @version V1.0
 */
public class ThriftConnectionPartition<T extends TServiceClient> implements Serializable {
	private static final long serialVersionUID = 1575062547601396682L;

	/**
	 * 空闲连接队列
	 */
	private BlockingQueue<ThriftConnectionHandle<T>> freeConnections;

	/**
	 * thrift服务器是否关闭的表识位
	 */
	private AtomicBoolean serverIsDown = new AtomicBoolean();

	/**
	 * 分区所绑定的服务器信息
	 */
	private ThriftServerInfo thriftServerInfo;

	/**
	 * 连接创建统计锁
	 */
	protected ReentrantReadWriteLock statsLock = new ReentrantReadWriteLock();

	/**
	 * 创建的连接数量
	 */
	private int createdConnections = 0;
	/**
	 * 分区支持的最大连接数
	 */
	private final int maxConnections;
	/**
	 * 分区支持的最小连接数
	 */
	private final int minConnections;
	/**
	 * 每批连接创建的数量
	 */
	private final int acquireIncrement;
	/**
	 * 连接检测操作信号处理队列
	 */
	private BlockingQueue<Object> poolWatchThreadSignalQueue = new ArrayBlockingQueue<Object>(1);

	/**
	 * true为不需要创建更多的连接 说明连接已经到了最大数量
	 */
	private boolean unableToCreateMoreTransactions = false;
	protected ReentrantReadWriteLock unableToCreateMoreTransactionsLock = new ReentrantReadWriteLock();

	public ThriftConnectionPartition(ThriftConnectionPool<T> thriftConnectionPool, ThriftServerInfo thriftServerInfo) {
		ThriftConnectionPoolConfig config = thriftConnectionPool.getConfig();
		this.thriftServerInfo = thriftServerInfo;
		this.maxConnections = config.getMaxConnectionPerServer();
		this.minConnections = config.getMinConnectionPerServer();
		this.acquireIncrement = config.getAcquireIncrement();
	}

	/**
	 * 设置空闲连接队列的方法
	 * 
	 * @param freeConnections
	 *            空闲连接队列
	 */
	protected void setFreeConnections(BlockingQueue<ThriftConnectionHandle<T>> freeConnections) {
		this.freeConnections = freeConnections;
	}

	/**
	 * 获取分区所对应的thrift服务器信息的方法
	 * 
	 * @return thrift服务器信息对象
	 */
	public ThriftServerInfo getThriftServerInfo() {
		return thriftServerInfo;
	}

	/**
	 * 添加空闲连接的方法
	 * 
	 * @param thriftConnectionHandle
	 *            thrift连接代理对象
	 * @throws ThriftConnectionPoolException
	 */
	public void addFreeConnection(ThriftConnectionHandle<T> thriftConnectionHandle)
			throws ThriftConnectionPoolException {
		thriftConnectionHandle.setOriginatingPartition(this);
		// 更新创建的连接数 创建数 +1
		updateCreatedConnections(1);

		if (!this.freeConnections.offer(thriftConnectionHandle)) {
			// 将连接放入队列失败 创建数 - 1
			updateCreatedConnections(-1);

			// 关闭原始连接
			thriftConnectionHandle.internalClose();
		}
	}

	/**
	 * 更新连接创建统计数的方法
	 * 
	 * @param increment
	 *            更新数量 增加或者减少
	 */
	protected void updateCreatedConnections(int increment) {

		try {
			this.statsLock.writeLock().lock();
			this.createdConnections += increment;
		} finally {
			this.statsLock.writeLock().unlock();
		}
	}

	/**
	 * 获取服务器是否关闭标识位的方法
	 * 
	 * @return 服务器是否关闭标识位
	 */
	public AtomicBoolean getServerIsDown() {
		return serverIsDown;
	}

	/**
	 * 设置连接可创建的状态
	 * 
	 * @param unableToCreateMoreTransactions
	 *            true为不能继续创建连接 false为可以继续创建连接
	 */
	public void setUnableToCreateMoreTransactions(boolean unableToCreateMoreTransactions) {
		try {
			unableToCreateMoreTransactionsLock.writeLock().lock();
			this.unableToCreateMoreTransactions = unableToCreateMoreTransactions;
		} finally {
			unableToCreateMoreTransactionsLock.writeLock().unlock();
		}
	}

	/**
	 * 获取是否能继续创建连接的方法
	 * 
	 * @return true为不能创建连接了 false为可以继续创建连接
	 */
	public boolean isUnableToCreateMoreTransactions() {
		return this.unableToCreateMoreTransactions;
	}

	/**
	 * 获取可用连接数量的方法
	 * 
	 * @return 可用连接数量
	 */
	public int getAvailableConnections() {
		return this.freeConnections.size();
	}

	/**
	 * 获取分区支持的最大连接数的方法
	 * 
	 * @return 分区支持的最大连接数
	 */
	public int getMaxConnections() {
		return this.maxConnections;
	}

	/**
	 * 获取连接分区最小的连接数的方法
	 * 
	 * @return 连接分区最小的连接数
	 */
	public int getMinConnections() {
		return this.minConnections;
	}

	/**
	 * 获取连接池检测信号队列的方法
	 * 
	 * @return 检测信号队列
	 */
	public BlockingQueue<Object> getPoolWatchThreadSignalQueue() {
		return this.poolWatchThreadSignalQueue;
	}

	/**
	 * 直接获取一个连接的方法
	 * 
	 * @return 连接代理对象
	 */
	protected ThriftConnection<T> poolFreeConnection() {
		ThriftConnection<T> result = this.freeConnections.poll();
		return result;
	}

	public ThriftConnection<T> poolFreeConnection(long timeout, TimeUnit unit) throws InterruptedException {
		ThriftConnection<T> result = this.freeConnections.poll(timeout, unit);
		return result;
	}

	/**
	 * 获取所有空闲连接的方法
	 * 
	 * @return 空闲连接队列
	 */
	public BlockingQueue<ThriftConnectionHandle<T>> getFreeConnections() {
		return this.freeConnections;
	}

	/**
	 * 获取创建的连接数量的方法
	 * 
	 * @return 分区创建的连接数
	 */
	public int getCreatedConnections() {
		try {
			this.statsLock.readLock().lock();
			return this.createdConnections;
		} finally {
			this.statsLock.readLock().unlock();
		}
	}

	/**
	 * 获取每批次连接创建数量的方法
	 * 
	 * @return 每批连接创建数量
	 */
	protected int getAcquireIncrement() {
		return this.acquireIncrement;
	}

}
