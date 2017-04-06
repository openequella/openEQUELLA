package com.tle.common.harvester;

import com.dytech.edge.exceptions.RuntimeApplicationException;

public class HarvesterException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	public HarvesterException(String message)
	{
		super(message);
		setShowStackTrace(false);
	}

	public HarvesterException(String message, Throwable cause)
	{
		super(message, cause);
		setShowStackTrace(false);
	}
}
