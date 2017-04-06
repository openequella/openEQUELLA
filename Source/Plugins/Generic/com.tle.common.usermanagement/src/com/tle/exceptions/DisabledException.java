package com.tle.exceptions;

public class DisabledException extends AuthenticationException
{

	public DisabledException(String msg)
	{
		super(msg);
	}

	public DisabledException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

}
