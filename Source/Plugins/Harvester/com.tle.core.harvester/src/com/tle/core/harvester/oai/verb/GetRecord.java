/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.verb;

import com.tle.core.harvester.oai.data.Record;
import com.tle.core.harvester.oai.data.Response;
import com.tle.core.harvester.oai.error.CannotDisseminateFormatException;
import com.tle.core.harvester.oai.error.IdDoesNotExistException;

/**
 *
 */
public class GetRecord extends Verb
{
	private static final String VERB = "GetRecord";

	public GetRecord(String identifier, String metadataPrefix)
	{
		addParamater(IDENTIFIER, identifier);
		addParamater(METADATA_PREFIX, metadataPrefix == null ? DC_PREFIX : metadataPrefix);
	}

	@Override
	public String getVerb()
	{
		return VERB;
	}

	public Record getResult() throws IdDoesNotExistException, CannotDisseminateFormatException
	{
		Response response = call();
		checkIdDoesNotExistError(response);
		checkCannotDisseminateFormat(response);
		return (Record) response.getMessage();
	}
}
