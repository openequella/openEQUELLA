/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.error;

import com.tle.core.harvester.oai.data.OAIError;

/**
 * 
 */
public class OAIException extends Exception
{
	protected String code;
	protected String errorMessage;

	public OAIException(String code, String message)
	{
		this.code = code;
		this.errorMessage = message;
	}

	public OAIException(String code, Throwable t)
	{
		super(t);
		this.code = code;
		this.errorMessage = t.getMessage();
	}

	public OAIException(OAIError error)
	{
		super(error.getMessage());
	}

	public String getCode()
	{
		return code;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}
}
