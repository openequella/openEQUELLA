package com.tle.webtests.framework;

public class EquellaErrorPageException extends RuntimeException
{
	public EquellaErrorPageException(String error, Throwable throwable)
	{
		super(error, throwable);
	}
}