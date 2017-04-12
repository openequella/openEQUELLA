/*
 * Created on Dec 7, 2004
 */
package com.dytech.edge.exceptions;

/**
 * @author Nicholas Read
 */
public class NotFoundException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;
	private boolean fromRequest;

	public NotFoundException(String message)
	{
		this(message, false);
	}

	public NotFoundException(String message, boolean fromRequest)
	{
		super(message);
		setShowStackTrace(false);
		this.fromRequest = fromRequest;
	}

	public NotFoundException(String message, Throwable cause, boolean fromRequest)
	{
		super(message, cause);
		setShowStackTrace(false);
		this.fromRequest = fromRequest;
	}

	public boolean isFromRequest()
	{
		return fromRequest;
	}

	public void setFromRequest(boolean fromRequest)
	{
		this.fromRequest = fromRequest;
	}

	@Override
	public boolean isShowStackTrace()
	{
		return false;
	}
}
