package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
public class InvalidSearchQueryException extends SearchingException
{
	private static final long serialVersionUID = 1L;

	public InvalidSearchQueryException(String query)
	{
		super(query);
	}

	public InvalidSearchQueryException(String query, Throwable t)
	{
		super(query, t);
	}

	@Override
	public String getLocalizedMessage()
	{
		return "The search query '" + getMessage() + "' is invalid"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
