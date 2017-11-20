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
