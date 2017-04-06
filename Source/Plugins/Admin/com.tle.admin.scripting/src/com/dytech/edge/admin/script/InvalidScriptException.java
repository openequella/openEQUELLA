package com.dytech.edge.admin.script;

public class InvalidScriptException extends Exception
{
	private static final long serialVersionUID = 1L;

	public InvalidScriptException(String message)
	{
		super(message);
	}

	public InvalidScriptException(String message, Throwable t)
	{
		super(message, t);
	}
}
