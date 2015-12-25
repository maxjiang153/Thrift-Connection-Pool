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

package com.wmz7year.thrift.pool.config;

import java.util.Arrays;

/**
 * 封装thrift信息的实体类<br>
 * 目前只有服务器地址、端口两个属性<br>
 * 日后考虑添加权重、备注一类的属性
 * 
 * @author jiangwei (ydswcy513@gmail.com)
 * @version V1.0
 */
public class ThriftServerInfo {
	/**
	 * 服务器地址
	 */
	private String host;
	/**
	 * 服务器端口
	 */
	private int port;
	/**
	 * 服务器ID
	 */
	private byte[] serverID;

	public ThriftServerInfo(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		this.serverID = null;
	}

	public ThriftServerInfo(String host, int port, byte[] serverID) {
		super();
		this.host = host;
		this.port = port;
		this.serverID = serverID;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public byte[] getServerID() {
		return serverID;
	}

	public void setServerID(byte[] serverID) {
		this.serverID = serverID;
	}

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		result = prime * result + Arrays.hashCode(serverID);
		return result;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThriftServerInfo other = (ThriftServerInfo) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		if (!Arrays.equals(serverID, other.serverID))
			return false;
		return true;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ThriftServerInfo [host=" + host + ", port=" + port + ", serverID=" + Arrays.toString(serverID) + "]";
	}

}
