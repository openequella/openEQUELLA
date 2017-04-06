package com.tle.web.workflow.tasks;

import com.dytech.edge.exceptions.QuietlyLoggable;

public class NotModeratingStepException extends RuntimeException implements QuietlyLoggable
{
	public NotModeratingStepException(String msg)
	{
		super(msg);
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
