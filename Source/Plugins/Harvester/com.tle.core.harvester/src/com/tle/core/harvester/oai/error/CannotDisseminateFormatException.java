/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.error;

import com.tle.core.harvester.oai.data.OAIError;

/**
 * 
 */
public class CannotDisseminateFormatException extends OAIException
{

	public CannotDisseminateFormatException(OAIError error)
	{
		super(error);
	}

	public CannotDisseminateFormatException(String format)
	{
		super("cannotDisseminateFormat", "The metadataPrefix '" + format + "' is not supported by this repository.");
	}
}
