package com.tle.core.notification.dao;

public class NotifiedUser
{
	private final String user;
	private final long instId;

	public NotifiedUser(String user, long instId)
	{
		this.user = user;
		this.instId = instId;
	}

	public String getUser()
	{
		return user;
	}

	public long getInstId()
	{
		return instId;
	}
}
