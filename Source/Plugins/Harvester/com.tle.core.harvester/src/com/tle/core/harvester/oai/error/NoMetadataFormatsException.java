/*
 * Created on Apr 13, 2005
 */
package com.tle.core.harvester.oai.error;

import com.tle.core.harvester.oai.data.OAIError;

/**
 * 
 */
public class NoMetadataFormatsException extends OAIException
{
	public NoMetadataFormatsException(OAIError error)
	{
		super(error);
	}

	public NoMetadataFormatsException()
	{
		super("noMetadataFormats", "No Metadata Formats available");
	}

	public NoMetadataFormatsException(Throwable t)
	{
		super("noMetadataFormats", t);
	}
}
