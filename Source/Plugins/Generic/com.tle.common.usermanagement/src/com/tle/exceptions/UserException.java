/*
 * Created on Dec 7, 2004
 */
package com.tle.exceptions;

import com.dytech.edge.exceptions.RuntimeApplicationException;

/**
 * @author Nicholas Read
 */
public class UserException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	public UserException(String message)
	{
		super(message);
	}

	public UserException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
