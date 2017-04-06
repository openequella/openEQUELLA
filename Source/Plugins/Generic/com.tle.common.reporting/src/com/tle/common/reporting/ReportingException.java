package com.tle.common.reporting;

public class ReportingException extends RuntimeException
{
	public enum Type
	{
		NODESIGNS
	}

	private final Type type;

	public ReportingException(String msg, Type type)
	{
		super(msg);
		this.type = type;
	}

	public Type getType()
	{
		return type;
	}
}
