/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.error;

import com.tle.core.harvester.oai.data.OAIError;

/**
 * 
 */
public class NoSetHierarchyException extends OAIException
{

	public NoSetHierarchyException(OAIError error)
	{
		super(error);
	}

	public NoSetHierarchyException()
	{
		super("noSetHierarchy", "Set hierarchy not supported by this repository");
	}

	public NoSetHierarchyException(Throwable t)
	{
		super("noSetHierarchy", t);
	}
}
