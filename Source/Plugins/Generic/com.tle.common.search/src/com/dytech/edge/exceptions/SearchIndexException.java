package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
public abstract class SearchIndexException extends SearchingException
{
	public SearchIndexException(String message)
	{
		super(message);
	}

	public SearchIndexException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
