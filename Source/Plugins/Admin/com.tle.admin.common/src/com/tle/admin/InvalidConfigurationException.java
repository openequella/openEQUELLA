package com.tle.admin;

/**
 * @author Nicholas Read
 */
public class InvalidConfigurationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public InvalidConfigurationException()
	{
		super();
	}

	public InvalidConfigurationException(String msg)
	{
		super(msg);
	}

	public InvalidConfigurationException(String msg, Throwable th)
	{
		super(msg, th);
	}

	public InvalidConfigurationException(Throwable th)
	{
		super(th);
	}
}
