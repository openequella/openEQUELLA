package com.tle.web.bulk.operation;

import java.io.Serializable;

public class BulkResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final boolean succeeded;
	private String name;
	private String reason;

	public BulkResult(boolean succeeded, String name, String reason)
	{
		this.name = name;
		this.reason = reason;
		this.succeeded = succeeded;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}

	public boolean isSucceeded()
	{
		return succeeded;
	}
}