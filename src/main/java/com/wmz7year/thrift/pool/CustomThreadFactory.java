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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责创建线程的工厂类<br>
 * 用于设置线程名称以及异常捕获
 * 
 * @Title: CustomThreadFactory.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 上午11:27:53
 * @version V1.0
 */
public class CustomThreadFactory implements ThreadFactory, UncaughtExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(CustomThreadFactory.class);

	/** 是否是守护线程的表识位 */
	private boolean daemon;
	/** 线程名称 */
	private String threadName;

	public CustomThreadFactory(String threadName, boolean daemon) {
		this.threadName = threadName;
		this.daemon = daemon;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, this.threadName);
		t.setDaemon(this.daemon);
		t.setUncaughtExceptionHandler(this);
		return t;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread,
	 *      java.lang.Throwable)
	 */
	public void uncaughtException(Thread thread, Throwable throwable) {
		logger.error("Uncaught Exception in thread " + thread.getName(), throwable);
	}

}
