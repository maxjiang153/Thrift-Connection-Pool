package com.wmz7year.thrift.pool.config;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.wmz7year.thrift.pool.example.Example;
import com.wmz7year.thrift.pool.example.Example.Client;

import junit.framework.TestCase;

public class TestSimpleExample extends TestCase {
	public void test() {
		TTransport transport = new TSocket("localhost", 9119);
		try {
			transport.open();
			TProtocol protocol = new TBinaryProtocol(transport);
			Client client = new Example.Client(protocol);
			client.ping();
			transport.close();
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}
}
