/*
 * Created on Apr 13, 2005
 */
package com.tle.core.harvester.oai.error;

@SuppressWarnings("nls")
public class BadArgumentException extends OAIException
{
	public BadArgumentException(String verb, String arg)
	{
		super("badArgument", "Verb '" + verb + "', " + arg);
	}
}
