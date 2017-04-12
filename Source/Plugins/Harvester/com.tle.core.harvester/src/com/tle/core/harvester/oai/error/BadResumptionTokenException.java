/*
 * Created on Apr 14, 2005
 */
package com.tle.core.harvester.oai.error;

@SuppressWarnings("nls")
public class BadResumptionTokenException extends OAIException
{
	public BadResumptionTokenException()
	{
		super("badResumptionToken", "The resumptionToken does not appear to be from this repository.");
	}
}
