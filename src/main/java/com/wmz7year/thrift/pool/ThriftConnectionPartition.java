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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.TServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wmz7year.thrift.pool.config.ThriftServerInfo;

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
	private static final Logger logger = LoggerFactory.getLogger(ThriftConnectionPartition.class);
	private static final long serialVersionUID = 1575062547601396682L;

	/**
	 * 连接池对象
	 */
	private ThriftConnectionPool<T> thriftConnectionPool;

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

	public ThriftConnectionPartition(ThriftConnectionPool<T> thriftConnectionPool, ThriftServerInfo thriftServerInfo) {
		this.thriftConnectionPool = thriftConnectionPool;
		this.thriftServerInfo = thriftServerInfo;
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
	 */
	public void addFreeConnection(ThriftConnectionHandle<T> thriftConnectionHandle) {
		// TODO Auto-generated method stub

	}

	/**
	 * 获取服务器是否关闭标识位的方法
	 * 
	 * @return 服务器是否关闭标识位
	 */
	public AtomicBoolean getServerIsDown() {
		return serverIsDown;
	}
}
