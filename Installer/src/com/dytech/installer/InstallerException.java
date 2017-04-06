package com.dytech.installer;

public class InstallerException extends RuntimeException
{
	public InstallerException()
	{
		super();
	}

	public InstallerException(String message)
	{
		super(message);
	}

	public InstallerException(Throwable cause)
	{
		super(cause);
	}

	public InstallerException(String message, Throwable cause)
	{
		super(message, cause);
	}
}