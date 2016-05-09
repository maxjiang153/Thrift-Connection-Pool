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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.thrift.TException;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wmz7year.thrift.pool.config.ThriftServerInfo;
import com.wmz7year.thrift.pool.example.Example;
import com.wmz7year.thrift.pool.example.Example.Iface;
import com.wmz7year.thrift.pool.example.Example.Processor;
import com.wmz7year.thrift.pool.example.Other;

import junit.framework.TestCase;

/**
 * 基础测试类<br>
 * 所有单元测试都需要继承该类进行测试<br>
 * 会在测试开始时自定义启动N个服务器<br>
 * 已经服务器关闭操作
 * 
 * @Title: BasicAbstractTest.java
 * @Package com.wmz7year.thrift.pool.config
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月19日 下午1:23:04
 * @version V1.0
 */
public abstract class BasicAbstractTest extends TestCase {
	protected static final Logger logger = LoggerFactory.getLogger(BasicAbstractTest.class);
	/**
	 * 最小服务器端口
	 */
	protected static final int MIN_PORT = 40000;
	/**
	 * 最大服务器端口
	 */
	protected static final int MAX_PORT = 41000;
	/**
	 * 本机地址
	 */
	protected static final String LOACLHOST = "127.0.0.1";

	/**
	 * 单元测试运行超时时间
	 */
	private static final long DFLT_TEST_TIMEOUT = 5 * 60 * 1000;

	/**
	 * 在停止单元测试的时候是否存在异常信息
	 */
	private boolean stopErr;

	/**
	 * 测试运行时间
	 */
	private static long ts = System.currentTimeMillis();

	/**
	 * 启动的服务器列表
	 */
	private List<TServer> servers = new ArrayList<TServer>();

	/**
	 * 在单元测试最开始时调用的方法
	 * 
	 * @throws Exception
	 *             当发生错误时抛出该异常
	 */
	protected abstract void beforeTest() throws Exception;

	/**
	 * 在单元测试最后调用的方法
	 * 
	 * @throws Exception
	 *             当发生错误时抛出该异常
	 */
	protected abstract void afterTest() throws Exception;

	/*
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		stopErr = false;

		try {
			beforeTest();
		} catch (Exception t) {
			t.printStackTrace();

			tearDown();
		}

		ts = System.currentTimeMillis();
	}

	/**
	 * 基础运行单元测试的方法
	 * 
	 * @throws Throwable
	 *             当发生错误时抛出该异常
	 */
	private void runTestInternal() throws Throwable {
		super.runTest();
	}

	/*
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest() throws Throwable {
		final AtomicReference<Throwable> ex = new AtomicReference<Throwable>();

		Thread runner = new Thread("test-runner") {
			@Override
			public void run() {
				try {
					runTestInternal();
				} catch (Throwable e) {
					ex.set(e);
				}
			}
		};

		runner.start();

		runner.join(isDebug() ? 0 : DFLT_TEST_TIMEOUT);

		if (runner.isAlive()) {
			throw new TimeoutException("单元测试运行时间超时 [测试名称：" + getName() + " 超时时间：" + DFLT_TEST_TIMEOUT + "]");
		}

		Throwable t = ex.get();
		if (t != null) {
			throw t;
		}

		assert !stopErr : "结束单元测试发生错误";
	}

	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		long dur = System.currentTimeMillis() - ts;
		logger.info("测试结束 测试名称：" + getName() + "  运行时间：" + dur + "ms");

		// 结束测试
		afterTest();

		// 关闭所有服务的方法
		stopAllServers();
	}

	/**
	 * 关闭所有thrift服务的方法
	 */
	protected void stopAllServers() {
		for (TServer tServer : servers) {
			tServer.stop();
		}
	}

	/**
	 * 判断是否是debug运行模式的方法<br>
	 * 依据-DDEBUG参数是否设置
	 * 
	 * @return true为debug模式 false为非debug模式
	 */
	protected boolean isDebug() {
		return System.getProperty("DEBUG") != null;
	}

	/**
	 * 启动本地服务器的方法<br>
	 * 为了方便测试使用
	 * 
	 * @param count
	 *            启动的服务器数量
	 * @return 启动的服务器信息列表
	 */
	protected List<ThriftServerInfo> startServers(int count) throws Exception {
		List<ThriftServerInfo> result = new ArrayList<ThriftServerInfo>();
		for (int i = 0; i < count; i++) {
			try {
				ThriftServerInfo startServer = startServer();
				result.add(startServer);
			} catch (Throwable e) {
				continue;
			}
		}
		return result;
	}
	
	/**
     * 启动本地服务器的方法<br>
     * 为了方便测试使用<br>
     * 该服务是thrift超时调用的服务
     * 
     * @param count
     *            启动的服务器数量
     * @return 启动的服务器信息列表
     */
    protected List<ThriftServerInfo> startTimeoutServers(int count) throws Exception {
        List<ThriftServerInfo> result = new ArrayList<ThriftServerInfo>();
        for (int i = 0; i < count; i++) {
            try {
                ThriftServerInfo startServer = startTimeoutServer();
                result.add(startServer);
            } catch (Throwable e) {
                continue;
            }
        }
        return result;
    }

	/**
	 * 启动多服务本地测试服务器的方法
	 * 
	 * @param count
	 *            启动的服务器数量
	 * @return 启动的服务器信息列表
	 */
	protected List<ThriftServerInfo> startMulitServiceServers(int count) throws Exception {
		List<ThriftServerInfo> result = new ArrayList<ThriftServerInfo>();
		for (int i = 0; i < count; i++) {
			try {
				ThriftServerInfo startServer = startMulitServiceServer();
				result.add(startServer);
			} catch (Throwable e) {
				continue;
			}
		}
		return result;
	}

	protected ThriftServerInfo startServer() throws Throwable {
		// 获取一个监听端口
		final int port = choseListenPort();
		ThriftServerInfo serverInfo = new ThriftServerInfo(LOACLHOST, port);
		final AtomicReference<Throwable> ex = new AtomicReference<Throwable>();

		Thread runner = new Thread("thrift-server-starter") {
			@Override
			public void run() {
				try {
					TServerTransport serverTransport = new TServerSocket(port);
					Factory proFactory = new TBinaryProtocol.Factory();
					Processor<Iface> processor = new Example.Processor<Example.Iface>(new Example.Iface() {

						@Override
						public void pong() throws TException {
							logger.info("pong");
						}

						@Override
						public void ping() throws TException {
							logger.info("ping");
						}
					});
					Args thriftArgs = new Args(serverTransport);
					thriftArgs.processor(processor);
					thriftArgs.protocolFactory(proFactory);
					TServer tserver = new TThreadPoolServer(thriftArgs);
					servers.add(tserver);
					logger.info("启动测试服务监听：" + port);
					tserver.serve();
				} catch (TTransportException e) {
					logger.error("thrift服务器启动失败", e);
					ex.set(e);
				}
			}
		};

		runner.start();

		Throwable throwable = ex.get();
		if (throwable != null) {
			throw throwable;
		}
		// 等待服务器启动
		Thread.sleep(1000);
		return serverInfo;
	}
	
	protected ThriftServerInfo startTimeoutServer() throws Throwable {
        // 获取一个监听端口
        final int port = choseListenPort();
        ThriftServerInfo serverInfo = new ThriftServerInfo(LOACLHOST, port);
        final AtomicReference<Throwable> ex = new AtomicReference<Throwable>();

        Thread runner = new Thread("thrift-timeout-server-starter") {
            @Override
            public void run() {
                try {
                    TServerTransport serverTransport = new TServerSocket(port);
                    Factory proFactory = new TBinaryProtocol.Factory();
                    Processor<Iface> processor = new Example.Processor<Example.Iface>(new Example.Iface() {

                        @Override
                        public void pong() throws TException {
                            try {
                                Thread.sleep(3100);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }

                        @Override
                        public void ping() throws TException {
                            try {
                                Thread.sleep(3100);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }
                    });
                    Args thriftArgs = new Args(serverTransport);
                    thriftArgs.processor(processor);
                    thriftArgs.protocolFactory(proFactory);
                    TServer tserver = new TThreadPoolServer(thriftArgs);
                    servers.add(tserver);
                    logger.info("启动测试服务监听：" + port);
                    tserver.serve();
                } catch (TTransportException e) {
                    logger.error("thrift服务器启动失败", e);
                    ex.set(e);
                }
            }
        };

        runner.start();

        Throwable throwable = ex.get();
        if (throwable != null) {
            throw throwable;
        }
        // 等待服务器启动
        Thread.sleep(1000);
        return serverInfo;
    }

	protected ThriftServerInfo startMulitServiceServer() throws Throwable {
		// 获取一个监听端口
		final int port = choseListenPort();
		ThriftServerInfo serverInfo = new ThriftServerInfo(LOACLHOST, port);
		final AtomicReference<Throwable> ex = new AtomicReference<Throwable>();
		// TODO
		Thread runner = new Thread("thrift-server-starter") {
			@Override
			public void run() {
				try {
					TMultiplexedProcessor processor = new TMultiplexedProcessor();
					TServerTransport serverTransport = new TServerSocket(port);
					Factory proFactory = new TBinaryProtocol.Factory();

					processor.registerProcessor("example", new Example.Processor<Example.Iface>(new Example.Iface() {

						@Override
						public void pong() throws TException {
							logger.info("example pong");
						}

						@Override
						public void ping() throws TException {
							logger.info("example ping");
						}
					}));

					processor.registerProcessor("other", new Other.Processor<Other.Iface>(new Other.Iface() {

						@Override
						public void pong() throws TException {
							logger.info("other pong");
						}

						@Override
						public void ping() throws TException {
							logger.info("other ping");
						}
					}));
					Args thriftArgs = new Args(serverTransport);
					thriftArgs.processor(processor);
					thriftArgs.protocolFactory(proFactory);
					TServer tserver = new TThreadPoolServer(thriftArgs);
					servers.add(tserver);
					logger.info("启动测试服务监听：" + port);
					tserver.serve();
				} catch (TTransportException e) {
					logger.error("thrift服务器启动失败", e);
					ex.set(e);
				}
			}
		};

		runner.start();

		Throwable throwable = ex.get();
		if (throwable != null) {
			throw throwable;
		}
		// 等待服务器启动
		Thread.sleep(1000);
		return serverInfo;
	}

	/**
	 * 获取一个可用端口的方法 return 可用的监听端口
	 */
	private int choseListenPort() {
		for (int port = MIN_PORT; port < MAX_PORT; port++) {
			if (checkPortNotInUse(port)) {
				return port;
			}
		}
		return 0;
	}

	/**
	 * 判断端口是否未使用的方法
	 * 
	 * @param port
	 *            需要校验的端口
	 * @return true为已使用 false为未使用
	 */
	private boolean checkPortNotInUse(int port) {
		try {
			new ServerSocket(port).close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
