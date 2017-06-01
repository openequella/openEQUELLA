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

import java.util.Iterator;
import java.util.Map;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.harvester.oai.data.Request;

/**
 * 
 */
public class OAIRequestConverter extends OAIAbstractConverter
{
	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(Request.class);
	}

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		Request rep = (Request) object;
		Map map = rep.getAttributes();
		Iterator i = map.keySet().iterator();
		while( i.hasNext() )
		{
			String key = i.next().toString();
			String[] value = (String[]) map.get(key);
			for( int j = 0; j < value.length; j++ )
			{
				writer.addAttribute(key, value[j]);
			}
		}

		writer.setValue(rep.getNode());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Request rep = new Request();

		return rep;
	}

}
