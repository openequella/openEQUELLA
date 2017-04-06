package com.dytech.edge.common;

import com.dytech.edge.exceptions.WorkflowException;

/**
 * This exception indicates that a resource has been locked by some entity.
 * 
 * @author Nicholas Read
 */
public class LockedException extends WorkflowException
{
	private static final long serialVersionUID = 1L;

	private final String userID;
	private final String sessionID;
	private final long entityId;

	public LockedException(String msg, String userID, String sessionID, long entityId)
	{
		super(msg);
		this.userID = userID;
		this.sessionID = sessionID;
		this.entityId = entityId;
	}

	public String getUserID()
	{
		return userID;
	}

	public String getSessionID()
	{
		return sessionID;
	}

	@Override
	public boolean isShowStackTrace()
	{
		return false;
	}

	@Override
	public boolean isSilent()
	{
		return false;
	}

	@Override
	public boolean isWarnOnly()
	{
		return true;
	}

	public long getEntityId()
	{
		return entityId;
	}
}
