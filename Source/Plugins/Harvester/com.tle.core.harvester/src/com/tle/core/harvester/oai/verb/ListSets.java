/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.verb;

import com.tle.core.harvester.oai.data.List;
import com.tle.core.harvester.oai.data.ResumptionToken;

/**
 * 
 */
public class ListSets extends Verb
{
	private static final String VERB = "ListSets";

	public ListSets()
	{
		//
	}

	public ListSets(ResumptionToken token)
	{
		addParamater(RESUMPTION_TOKEN, token.getToken());
	}

	public ListSets(String resumptionToken)
	{
		addParamater(RESUMPTION_TOKEN, resumptionToken);
	}

	@Override
	public String getVerb()
	{
		return VERB;
	}

	public List getResult()
	{
		return listFromXML(call());
	}
}
