/*
 * Created on Sep 20, 2005
 */
package com.tle.common.search.whereparser;

import com.dytech.edge.exceptions.RuntimeApplicationException;

public class InvalidWhereException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	public InvalidWhereException(String message)
	{
		super(message);
	}

	public InvalidWhereException(String message, Throwable t)
	{
		super(message, t);
	}

	@Override
	public boolean isShowStackTrace()
	{
		return false;
	}
}
