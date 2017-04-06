package com.tle.core.libav;

import com.dytech.edge.exceptions.RuntimeApplicationException;

/**
 * It's runtime, so you don't have to catch it! Bonus!
 * 
 * @author Aaron
 */
public class LibAvException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;

	public LibAvException(String message)
	{
		super(message);
		setLogged(true);
	}

	public LibAvException(String message, Throwable t)
	{
		super(message, t);
		setLogged(true);
	}
}