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
import com.tle.core.harvester.oai.data.List;
import com.tle.core.harvester.oai.data.OAIError;
import com.tle.core.harvester.oai.data.Request;
import com.tle.core.harvester.oai.data.Response;

/**
 * 
 */
public class OAIResponseConverter extends OAIAbstractConverter
{
	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(Response.class);
	}

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		Response rep = (Response) object;
		writer.addAttribute("xmlns", "http://www.openarchives.org/OAI/2.0/");
		writer.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		writer.addAttribute("xsi:schemaLocation",
			"http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd");

		startNode(writer, "responseDate", rep.getResponseDate());
		marshal(rep.getRequest(), writer);

		Object message = rep.getMessage();
		if( message instanceof List )
		{
			List col = (List) message;
			startNode(writer, rep.getMessageNodeName(), col, true);
		}
		else
		{
			marshal(rep.getMessage(), writer);
		}
		marshal(rep.getError(), writer);
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Response rep = new Response();
		for( ; reader.hasMoreChildren(); reader.moveUp() )
		{
			reader.moveDown();
			String name = reader.getNodeName();

			if( name.equals("responseDate") )
			{
				String value = reader.getValue();
				rep.setResponseDate(value);
			}
			else if( name.equals("error") )
			{
				rep.setError((OAIError) convert(name, context));
			}
			else if( name.equals("request") )
			{
				rep.setRequest((Request) convert(name, context));
			}
			else if( name.equals("GetRecord") )
			{
				rep.setMessageNodeName(name);
				reader.moveDown();
				rep.setMessage(convert(reader.getNodeName(), context));
				reader.moveUp();
			}
			else if( name.equals("Identify") )
			{
				rep.setMessageNodeName(name);
				rep.setMessage(convert(name, context));
			}
			else
			{
				List list = new List();
				for( ; reader.hasMoreChildren(); reader.moveUp() )
				{
					reader.moveDown();
					Object o = convert(reader.getNodeName(), context);
					list.add(o);
				}
				rep.setMessage(list);
			}
		}
		return rep;
	}

}
