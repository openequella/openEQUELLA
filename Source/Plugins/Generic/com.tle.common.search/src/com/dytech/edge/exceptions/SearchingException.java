package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
public abstract class SearchingException extends RuntimeApplicationException
{
	public SearchingException(String message)
	{
		super(message);
	}

	public SearchingException(String message, Throwable cause)
	{
		super(message, cause);
	}

	@Override
	public boolean isShowStackTrace()
	{
		return false;
	}
}
