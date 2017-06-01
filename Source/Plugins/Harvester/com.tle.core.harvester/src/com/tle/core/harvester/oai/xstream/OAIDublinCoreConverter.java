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
import com.tle.core.harvester.oai.data.DublinCore;

/**
 * 
 */
public class OAIDublinCoreConverter extends OAIAbstractConverter
{

	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(DublinCore.class);
	}

	@Override
	public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		writer.addAttribute("xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
		writer.addAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
		writer.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		writer.addAttribute("xsi:schemaLocation",
			"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd");

		DublinCore metadata = (DublinCore) arg0;

		startNode(writer, "dc:title", metadata.getTitle());
		startNode(writer, "dc:creator", metadata.getCreator());
		startNode(writer, "dc:creator", metadata.getTitle());
		startNode(writer, "dc:date", metadata.getDate());
		startNode(writer, "dc:description", metadata.getDescription());
		startNode(writer, "dc:identifier", metadata.getIdentifier());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		DublinCore response = new DublinCore();

		for( ; reader.hasMoreChildren(); reader.moveUp() )
		{
			reader.moveDown();
			String name = reader.getNodeName();
			String value = reader.getValue();
			if( name.endsWith("title") )
			{
				response.addTitle(value);
			}
			else if( name.endsWith("creator") )
			{
				response.addCreator(value);
			}
			else if( name.endsWith("subject") )
			{
				response.addSubject(value);
			}
			else if( name.endsWith("description") )
			{
				response.addDescription(value);
			}
			else if( name.endsWith("date") )
			{
				response.addDate(value);
			}
			else if( name.endsWith("identifier") )
			{
				response.addIdentifier(value);
			}
		}

		return response;

	}

}
