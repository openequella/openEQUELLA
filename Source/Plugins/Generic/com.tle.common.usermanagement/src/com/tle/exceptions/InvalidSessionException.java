package com.tle.exceptions;

public class InvalidSessionException extends AuthenticationException
{

	public InvalidSessionException(String msg, Throwable t)
	{
		super(msg, t);
	}

	public InvalidSessionException(String msg)
	{
		super(msg);
	}

}
