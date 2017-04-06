package com.tle.core.services.impl;

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
