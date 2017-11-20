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

package com.tle.core.harvester.oai.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.harvester.oai.data.ResumptionToken;

/**
 * 
 */
public class OAIResumptionTokenConverter extends OAIAbstractConverter
{
	private static final String CURSOR = "cursor";
	private static final String COMPLETELISTSIZE = "completeListSize";

	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(ResumptionToken.class);
	}

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		ResumptionToken token = (ResumptionToken) object;
		writer.addAttribute(COMPLETELISTSIZE, "" + token.getCompleteListSize());
		writer.addAttribute(CURSOR, "" + token.getCursor());
		writer.setValue(token.getToken());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		ResumptionToken token = new ResumptionToken();
		String size = reader.getAttribute(COMPLETELISTSIZE);
		String cursor = reader.getAttribute(CURSOR);
		token.setCompleteListSize(parseInt(size, 0));
		token.setCursor(parseInt(cursor, 0));
		token.setToken(reader.getValue());
		return token;
	}

}
