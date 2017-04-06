package com.tle.core.harvester.old.dsoap;

public class SoapTimeoutException extends Exception
{
	public SoapTimeoutException()
	{
		super();
	}

	public SoapTimeoutException(String msg)
	{
		super(msg);
	}
}