/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.data;

/**
 * 
 */
public class OAIError
{
	private String code;
	private String message;

	public OAIError(String code2, String message2)
	{
		this.code = code2;
		this.message = message2;
		if( message == null )
		{
			message = ""; //$NON-NLS-1$
		}

		if( code == null )
		{
			code = ""; //$NON-NLS-1$
		}
	}

	public String getCode()
	{
		return code;
	}

	public String getMessage()
	{
		return message;
	}
}
