package com.wmz7year.thrift.pool.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * thrift连接池相关的异常对象
 * 
 * @Title: ThriftConnectionPoolException.java
 * @Package com.wmz7year.thrift.pool.exception
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 上午9:57:55
 * @version V1.0
 */
public class ThriftConnectionPoolException extends Exception {
	private static final long serialVersionUID = 6276176462644093117L;

	private Throwable nestedThrowable = null;

	public ThriftConnectionPoolException() {
		super();
	}

	public ThriftConnectionPoolException(String msg) {
		super(msg);
	}

	public ThriftConnectionPoolException(Throwable nestedThrowable) {
		this.nestedThrowable = nestedThrowable;
	}

	public ThriftConnectionPoolException(String msg, Throwable nestedThrowable) {
		super(msg);
		this.nestedThrowable = nestedThrowable;
	}

	@Override
	public void printStackTrace() {
		super.printStackTrace();
		if (nestedThrowable != null) {
			nestedThrowable.printStackTrace();
		}
	}

	@Override
	public void printStackTrace(PrintStream ps) {
		super.printStackTrace(ps);
		if (nestedThrowable != null) {
			nestedThrowable.printStackTrace(ps);
		}
	}

	@Override
	public void printStackTrace(PrintWriter pw) {
		super.printStackTrace(pw);
		if (nestedThrowable != null) {
			nestedThrowable.printStackTrace(pw);
		}
	}
}
