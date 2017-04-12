package com.dytech.edge.exceptions;

import com.tle.common.i18n.CurrentLocale;

/**
 * Same as WorkflowOperation, but without having to put annoying
 * CurrentLocale.get(blah) everywhere.
 * 
 * @author aholland
 */
public class OperationException extends WorkflowException
{
	public OperationException(Throwable t)
	{
		super(t);
	}

	public OperationException(Throwable t, String key, Object... params)
	{
		super(CurrentLocale.get(key, params), t);
	}

	public OperationException(String key, Object... params)
	{
		super(CurrentLocale.get(key, params));
	}
}
