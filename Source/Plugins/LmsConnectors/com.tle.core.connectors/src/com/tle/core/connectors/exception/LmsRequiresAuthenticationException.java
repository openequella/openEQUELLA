package com.tle.core.connectors.exception;

/**
 * @author Aaron
 *
 */
public class LmsRequiresAuthenticationException extends RuntimeException
{
	public LmsRequiresAuthenticationException(String msg)
	{
		super(msg);
	}
}
