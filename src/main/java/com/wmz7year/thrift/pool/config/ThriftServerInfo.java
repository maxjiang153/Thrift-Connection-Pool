package com.wmz7year.thrift.pool.config;

/**
 * 封装thrift信息的实体类<br>
 * 目前只有服务器地址、端口两个属性<br>
 * 日后考虑添加权重、备注一类的属性
 * 
 * @Title: ThriftServerInfo.java
 * @Package com.wmz7year.thrift.pool.config
 * @author jiangwei (ydswcy513@gmail.com)
 * @date 2015年11月18日 上午10:54:42
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

	public ThriftServerInfo(String host, int port) {
		super();
		this.host = host;
		this.port = port;
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

	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
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
		return true;
	}

}
