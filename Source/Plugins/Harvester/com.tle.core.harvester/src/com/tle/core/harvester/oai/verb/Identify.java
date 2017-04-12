/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.verb;

import com.tle.core.harvester.oai.data.Repository;
import com.tle.core.harvester.oai.data.Response;

/**
 * 
 */
public class Identify extends Verb
{
	private static final String VERB = "Identify";

	@Override
	public String getVerb()
	{
		return VERB;
	}

	public Repository getResult()
	{
		Response response = call();
		return (Repository) response.getMessage();
	}
}
