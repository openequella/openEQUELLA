/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.verb;

import com.tle.core.harvester.oai.data.List;
import com.tle.core.harvester.oai.data.Response;
import com.tle.core.harvester.oai.data.ResumptionToken;
import com.tle.core.harvester.oai.error.IdDoesNotExistException;
import com.tle.core.harvester.oai.error.NoMetadataFormatsException;

/**
 *
 */
public class ListMetadataFormats extends Verb
{
	private static final String VERB = "ListMetadataFormats";

	public ListMetadataFormats()
	{
		super();
	}

	public ListMetadataFormats(ResumptionToken token)
	{
		addParamater(RESUMPTION_TOKEN, token.getToken());
	}

	public ListMetadataFormats(String identifier)
	{
		addParamater(IDENTIFIER, identifier);
	}

	@Override
	public String getVerb()
	{
		return VERB;
	}

	public List getResult() throws IdDoesNotExistException, NoMetadataFormatsException
	{
		Response response = call();
		checkIdDoesNotExistError(response);
		checkNoMetadataFormats(response);
		return listFromXML(response);
	}
}
