package com.tle.core.email;

public class EmailResult<T>
{
	private final Throwable error;
	private final T key;

	public EmailResult(Throwable t, T key)
	{
		this.error = t;
		this.key = key;
	}

	public boolean isSuccessful()
	{
		return error == null;
	}

	public Throwable getError()
	{
		return error;
	}

	public T getKey()
	{
		return key;
	}

}