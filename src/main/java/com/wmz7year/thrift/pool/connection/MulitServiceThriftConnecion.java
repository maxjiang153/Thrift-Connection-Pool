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

package com.wmz7year.thrift.pool.connection;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.TProtocolType;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/**
 * 多服务支持的thrift连接对象
 * 
 * @author jiangwei (ydswcy513@gmail.com)
 * @version V1.0
 */
public class MulitServiceThriftConnecion<T extends TServiceClient> implements ThriftConnection<T> {
	private static final Logger logger = LoggerFactory.getLogger(MulitServiceThriftConnecion.class);
	/**
	 * 服务器地址
	 */
	private String host;
	/**
	 * 服务器端口
	 */
	private int port;
	/**
	 * 连接超时时间
	 */
	private int connectionTimeOut;
	/**
	 * thrift管道类型
	 */
	private TProtocolType tProtocolType;

	/**
	 * thrift客户端对象
	 */
	private Map<String, Class<? extends TServiceClient>> thriftClientClasses;

	/**
	 * thrift连接对象
	 */
	private TTransport transport;

	/**
	 * 实例化后的客户端对象
	 */
	private Map<String, T> clients = new HashMap<String, T>();

	public MulitServiceThriftConnecion(String host, int port, int connectionTimeOut, TProtocolType tProtocolType,
			Map<String, Class<? extends TServiceClient>> thriftClientClasses) throws ThriftConnectionPoolException {
		this.host = host;
		this.port = port;
		this.connectionTimeOut = connectionTimeOut;
		this.tProtocolType = tProtocolType;
		this.thriftClientClasses = thriftClientClasses;

		// 创建连接
		createConnection();
	}

	/**
	 * 创建原始连接的方法
	 * 
	 * @throws ThriftConnectionPoolException
	 *             创建连接出现问题时抛出该异常
	 */
	@SuppressWarnings("unchecked")
	private void createConnection() throws ThriftConnectionPoolException {
		try {
			transport = new TSocket(host, port, connectionTimeOut);
			transport.open();
			TProtocol protocol = createTProtocol(transport);

			Iterator<Entry<String, Class<? extends TServiceClient>>> iterator = thriftClientClasses.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Entry<String, Class<? extends TServiceClient>> entry = iterator.next();
				String serviceName = entry.getKey();
				Class<? extends TServiceClient> clientClass = entry.getValue();
				TMultiplexedProtocol multiProtocol = new TMultiplexedProtocol(protocol, serviceName);
				// 反射实例化客户端对象
				Constructor<? extends TServiceClient> clientConstructor = clientClass.getConstructor(TProtocol.class);
				T client = (T) clientConstructor.newInstance(multiProtocol);
				clients.put(serviceName, client);
				if (logger.isDebugEnabled()) {
					logger.debug("创建新连接成功:" + host + " 端口：" + port);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ThriftConnectionPoolException("无法连接服务器：" + host + " 端口：" + port, e);
		}
	}

	/*
	 * @see com.wmz7year.thrift.pool.connection.ThriftConnection#getClient()
	 */
	@Override
	public T getClient() {
		throw new UnsupportedOperationException("多服务情况下不允许调用单服务获取客户端的方法");
	}

	/*
	 * @see
	 * com.wmz7year.thrift.pool.connection.ThriftConnection#getClient(java.lang.
	 * String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <K extends TServiceClient> K getClient(String serviceName, Class<K> clazz) {
		T serviceClient = this.clients.get(serviceName);
		if (serviceClient == null) {
			throw new IllegalArgumentException("未知的服务名称：" + serviceName);
		}

		return (K) serviceClient;
	}

	/*
	 * @see com.wmz7year.thrift.pool.connection.ThriftConnection#isClosed()
	 */
	@Override
	public boolean isClosed() {
		return !transport.isOpen();
	}

	/**
	 * 根据配置创建thrift管道的方法
	 * 
	 */
	private TProtocol createTProtocol(TTransport transport) {
		if (tProtocolType == TProtocolType.BINARY) {
			return new TBinaryProtocol(transport);
		} else if (tProtocolType == TProtocolType.JSON) {
			return new TJSONProtocol(transport);
		}
		throw new IllegalStateException("暂不支持的管道类型：" + tProtocolType);
	}

	/*
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		if (transport != null) {
			transport.close();
		}
	}

	/**
	 * 获取多服务模式下所有thrift客户端的方法
	 * 
	 * @return 多服务下所有thrift客户端集合
	 */
	public Map<String, T> getMuiltServiceClients() {
		return this.clients;
	}

}
