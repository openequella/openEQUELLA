package com.tle.web.sections;

public class SectionsRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public SectionsRuntimeException(String msg)
	{
		super(msg);
	}

	public SectionsRuntimeException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	public SectionsRuntimeException(Throwable cause)
	{
		super(cause);
	}
}
