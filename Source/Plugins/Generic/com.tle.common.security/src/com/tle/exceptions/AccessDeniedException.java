package com.tle.exceptions;

import com.dytech.edge.exceptions.QuietlyLoggable;

public class AccessDeniedException extends NestedRuntimeException implements QuietlyLoggable
{
	public AccessDeniedException(String msg)
	{
		super(msg);
	}

	public AccessDeniedException(String msg, Throwable t)
	{
		super(msg, t);
	}

	@Override
	public boolean isSilent()
	{
		return false;
	}

	@Override
	public boolean isShowStackTrace()
	{
		return false;
	}

	@Override
	public boolean isWarnOnly()
	{
		return true;
	}
}
