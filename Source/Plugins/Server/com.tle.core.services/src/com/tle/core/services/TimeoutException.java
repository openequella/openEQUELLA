package com.tle.core.services;

public class TimeoutException extends RuntimeException
{
	public TimeoutException(String msg)
	{
		super(msg);
	}

	private static final long serialVersionUID = 1L;

}
