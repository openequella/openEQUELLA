package com.tle.client.harness;

/**
 * @author nread
 */
public class ServerProfile
{
	private String name;
	private String server;
	private String username;
	private String password;

	public ServerProfile()
	{
		super();
	}

	public String getName()
	{
		return name;
	}

	public String getPassword()
	{
		return password;
	}

	public String getServer()
	{
		return server;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setServer(String server)
	{
		this.server = server;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}
}
