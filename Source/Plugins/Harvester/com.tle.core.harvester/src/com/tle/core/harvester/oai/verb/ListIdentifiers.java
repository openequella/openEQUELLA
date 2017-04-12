/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.verb;

import com.tle.core.harvester.oai.data.List;
import com.tle.core.harvester.oai.data.Response;
import com.tle.core.harvester.oai.data.ResumptionToken;
import com.tle.core.harvester.oai.error.CannotDisseminateFormatException;
import com.tle.core.harvester.oai.error.NoRecordsMatchException;
import com.tle.core.harvester.oai.error.NoSetHierarchyException;

/**
 * 
 */
public class ListIdentifiers extends Verb
{
	private static final String SET = "set";
	private static final String FROM = "from";
	private static final String UNTIL = "until";
	private static final String VERB = "ListIdentifiers";

	public ListIdentifiers(String set, String from, String until, String metadataPrefix)
	{
		addParamater(SET, set);
		addParamater(FROM, from);
		addParamater(UNTIL, until);
		addParamater(METADATA_PREFIX, metadataPrefix == null ? DC_PREFIX : metadataPrefix);
	}

	public ListIdentifiers(ResumptionToken resumptionToken)
	{
		addParamater(RESUMPTION_TOKEN, resumptionToken.getToken());
	}

	@Override
	public String getVerb()
	{
		return VERB;
	}

	public List getResult() throws NoRecordsMatchException, NoSetHierarchyException, CannotDisseminateFormatException
	{
		Response response = call();
		checkNoRecordsMatch(response);
		checkNoSetHierarchy(response);
		checkCannotDisseminateFormat(response);
		return listFromXML(response);
	}
}
