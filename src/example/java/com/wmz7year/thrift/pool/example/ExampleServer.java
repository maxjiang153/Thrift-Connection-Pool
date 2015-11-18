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

package com.wmz7year.thrift.pool.example;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import com.wmz7year.thrift.pool.example.Example.Iface;
import com.wmz7year.thrift.pool.example.Example.Processor;

/**
 * 测试thrift服务器
 * 
 * @Title: ExampleServer.java
 * @Package com.wmz7year.thrift.pool.example
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 上午9:48:15
 * @version V1.0
 */
public class ExampleServer {
	public static void main(String[] args) {
		try {
			int port = 9119;
			TServerTransport serverTransport = new TServerSocket(port);
			Factory proFactory = new TBinaryProtocol.Factory();
			Processor<Iface> processor = new Example.Processor<Example.Iface>(new Example.Iface() {

				@Override
				public void pong() throws TException {
					System.out.println("pong");
				}

				@Override
				public void ping() throws TException {
					System.out.println("ping");
				}
			});
			Args thriftArgs = new Args(serverTransport);
			thriftArgs.processor(processor);
			thriftArgs.protocolFactory(proFactory);
			TServer tserver = new TThreadPoolServer(thriftArgs);
			System.out.println("启动监听:" + port);
			tserver.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}
}
