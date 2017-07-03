package com.tle.core.cluster.exception;

public class MessagingException extends RuntimeException
{
	public MessagingException(String error)
	{
		super(error);
	}

	public MessagingException(String msg, Throwable t)
	{
		super(msg, t);
	}
}
