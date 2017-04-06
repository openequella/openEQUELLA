package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
public class DRMException extends RuntimeApplicationException
{
	public DRMException(String message)
	{
		super(message);
		setShowStackTrace(false);
	}
}
