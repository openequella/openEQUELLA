package com.tle.common.scripting;

/**
 * @author Aaron
 */
public class ScriptException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ScriptException(Exception wrapped)
	{
		super(wrapped);
	}
}
