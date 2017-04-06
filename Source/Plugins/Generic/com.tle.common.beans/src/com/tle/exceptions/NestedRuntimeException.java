package com.tle.exceptions;

public abstract class NestedRuntimeException extends RuntimeException
{
	public NestedRuntimeException(String msg)
	{
		super(msg);
	}

	public NestedRuntimeException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	@SuppressWarnings("nls")
	@Override
	public String getMessage()
	{
		Throwable cause = getCause();
		String message = super.getMessage();
		if( cause != null )
		{
			StringBuilder buf = new StringBuilder();
			if( message != null )
			{
				buf.append(message).append("; ");
			}
			buf.append("nested exception is ").append(cause);
			return buf.toString();
		}
		else
		{
			return message;
		}
	}
}
