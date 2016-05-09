/**
 * Copyright 2015 Jiang Wei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wmz7year.thrift.pool;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.transport.TTransportException;

import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig;
import com.wmz7year.thrift.pool.config.ThriftConnectionPoolConfig.TProtocolType;
import com.wmz7year.thrift.pool.config.ThriftServerInfo;
import com.wmz7year.thrift.pool.connection.ThriftConnection;
import com.wmz7year.thrift.pool.example.Example;
import com.wmz7year.thrift.pool.exception.ThriftConnectionPoolException;

/*
 * 服务端响应超时的单元测试
 */
public class TimeoutConnectionTest extends BasicAbstractTest {

    private List<ThriftServerInfo> servers;

    /*
     * @see com.wmz7year.thrift.pool.BasicAbstractTest#beforeTest()
     */
    @Override
    protected void beforeTest() throws Exception {
        servers = startTimeoutServers(1);
        // TODO

    }

    /*
     * @see com.wmz7year.thrift.pool.BasicAbstractTest#afterTest()
     */
    @Override
    protected void afterTest() throws Exception {
        // ignore
    }

    public void testTimeoutService() throws Exception {
        ThriftConnectionPoolConfig config = new ThriftConnectionPoolConfig();
        config.setConnectTimeout(3000);
        config.setThriftProtocol(TProtocolType.BINARY);
        // 该端口不存在
        for (ThriftServerInfo thriftServerInfo : servers) {
            config.addThriftServer(thriftServerInfo.getHost(), thriftServerInfo.getPort());
        }
        config.setClientClass(Example.Client.class);

        config.setMaxConnectionPerServer(2);
        config.setMinConnectionPerServer(1);
        config.setIdleMaxAge(2, TimeUnit.SECONDS);
        config.setMaxConnectionAge(2);
        config.setLazyInit(false);
        config.setAcquireIncrement(2);
        config.setAcquireRetryDelay(2000);

        config.setAcquireRetryAttempts(1);
        config.setMaxConnectionCreateFailedCount(1);
        config.setConnectionTimeoutInMs(50000);

        config.check();

        final ThriftConnectionPool<TServiceClient> pool =
            new ThriftConnectionPool<TServiceClient>(config);

        final int threadCount = 2;
        CountDownLatch countDown = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < threadCount; i++) {
                        ThriftConnection<TServiceClient> connection = null;
                        try {
                            connection = pool.getConnection();
                            // example service
                            com.wmz7year.thrift.pool.example.Example.Client exampleServiceClient =
                                connection.getClient("example",
                                    Example.Client.class);
                            exampleServiceClient.ping();
                            connection.close();
                        } catch (TTransportException e) {
                            logger.info("set time out connection unavailable");
                            connection.setAvailable(false);
                        } catch (ThriftConnectionPoolException e) {
                            e.printStackTrace();
                        } catch (TException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (connection != null) {
                                try {
                                    connection.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }
            }).start();
        }

        countDown.await();

        pool.close();
    }

}
