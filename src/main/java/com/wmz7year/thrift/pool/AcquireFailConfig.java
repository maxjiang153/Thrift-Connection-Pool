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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接获取失败时重试配置
 * 
 * @Title: AcquireFailConfig.java
 * @Package com.wmz7year.thrift.pool
 * @author jiangwei (jiangwei@1318.com)
 * @date 2015年11月18日 下午2:01:47
 * @version V1.0
 */
public class AcquireFailConfig {
	/**
	 * 记录日志的内容
	 */
	private String logMessage = "";
	/**
	 * 重试次数
	 */
	private AtomicInteger acquireRetryAttempts = new AtomicInteger();
	/**
	 * 重试等待时间
	 */
	private long acquireRetryDelayInMs;

	public void setAcquireRetryAttempts(AtomicInteger acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
	}

	public void setAcquireRetryDelayInMs(long acquireRetryDelayInMs) {
		this.acquireRetryDelayInMs = acquireRetryDelayInMs;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public AtomicInteger getAcquireRetryAttempts() {
		return acquireRetryAttempts;
	}

	public long getAcquireRetryDelayInMs() {
		return acquireRetryDelayInMs;
	}

}
