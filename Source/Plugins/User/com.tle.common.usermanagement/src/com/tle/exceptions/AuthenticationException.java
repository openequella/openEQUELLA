package com.tle.exceptions;

import com.dytech.edge.exceptions.QuietlyLoggable;
import com.tle.common.beans.exception.NestedRuntimeException;

public class AuthenticationException extends NestedRuntimeException implements QuietlyLoggable
{
	public AuthenticationException(String msg)
	{
		super(msg);
	}

	public AuthenticationException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	@Override
	public boolean isShowStackTrace()
	{
		return false;
	}

	@Override
	public boolean isSilent()
	{
		return false;
	}

	@Override
	public boolean isWarnOnly()
	{
		return true;
	}
}
