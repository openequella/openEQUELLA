package com.tle.common.remotesqlquerying;

public class ConnectionDetails
{
	private final String driverClass;
	private final String jdbcUrl;
	private final String username;
	private final String password;

	public ConnectionDetails(String driverClass, String jdbcUrl, String username, String password)
	{
		this.driverClass = driverClass;
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
	}

	public String getDriverClass()
	{
		return driverClass;
	}

	public String getJdbcUrl()
	{
		return jdbcUrl;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}
}
