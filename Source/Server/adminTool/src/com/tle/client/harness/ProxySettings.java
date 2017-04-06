/*
 * Created on Jan 13, 2005
 */
package com.tle.client.harness;

/**
 * @author Nicholas Read
 */
public class ProxySettings
{
	private String host;
	private int port;
	private String username;
	private String password;

	public ProxySettings()
	{
		super();
	}

	/**
	 * @return Returns the host.
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * @param host The host to set.
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return Returns the port.
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * @param port The port to set.
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * @return Returns the username.
	 */
	public String getUsername()
	{
		return username;
	}

	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}
}
