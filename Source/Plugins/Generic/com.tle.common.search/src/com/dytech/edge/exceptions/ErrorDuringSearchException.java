package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
public class ErrorDuringSearchException extends SearchingException
{
	private static final long serialVersionUID = 1L;

	public ErrorDuringSearchException(String query, Throwable cause)
	{
		super("An error occurred during a search for '" + query + '\'', cause);
	}
}
