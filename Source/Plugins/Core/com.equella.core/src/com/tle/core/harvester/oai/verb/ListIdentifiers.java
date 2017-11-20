/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
