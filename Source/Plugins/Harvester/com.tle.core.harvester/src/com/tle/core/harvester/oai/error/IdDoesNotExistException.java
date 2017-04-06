/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.error;

import com.tle.core.harvester.oai.data.OAIError;

/**
 * 
 */
public class IdDoesNotExistException extends OAIException
{
	public IdDoesNotExistException(OAIError message)
	{
		super(message);
	}

	public IdDoesNotExistException(String id)
	{
		super("idDoesNotExist", "The identifier '" + id + "' does not correspond to an item in this repository.");
	}

}
