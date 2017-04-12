package com.tle.exceptions;

public class AccountExpiredException extends AuthenticationException
{

	public AccountExpiredException(String msg)
	{
		super(msg);
	}

	public AccountExpiredException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

}
