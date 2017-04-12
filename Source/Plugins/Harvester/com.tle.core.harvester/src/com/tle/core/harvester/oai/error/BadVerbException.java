/*
 * Created on Apr 13, 2005
 */
package com.tle.core.harvester.oai.error;

@SuppressWarnings("nls")
public class BadVerbException extends OAIException
{
	public BadVerbException(String verb)
	{
		super("badVerb", "Bad verb. Verb '" + verb + "' not implemented.");
	}

	public BadVerbException()
	{
		super("badVerb", "No verb specified!");
	}
}
