/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.error;

import com.tle.core.harvester.oai.data.OAIError;

/**
 * 
 */
public class NoRecordsMatchException extends OAIException
{

	public NoRecordsMatchException(OAIError error)
	{
		super(error);
	}

	public NoRecordsMatchException()
	{
		super("noRecordsMatch", "No records match");
	}

	public NoRecordsMatchException(Throwable t)
	{
		super("noRecordsMatch", t);
	}
}
