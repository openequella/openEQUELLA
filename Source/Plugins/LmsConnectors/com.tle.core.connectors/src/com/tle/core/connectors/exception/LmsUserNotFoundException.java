package com.tle.core.connectors.exception;

/**
 * @author Aaron
 */
public class LmsUserNotFoundException extends Exception
{
	private static final long serialVersionUID = 1L;

	private final String username;

	public LmsUserNotFoundException(String username, String message)
	{
		super(message);
		this.username = username;
	}

	public String getUsername()
	{
		return username;
	}
}
