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
import com.tle.core.harvester.oai.data.MetadataFormat;

public class OAIMetadataFormatConverter extends OAIAbstractConverter
{

	@SuppressWarnings("nls")
	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		MetadataFormat format = (MetadataFormat) object;
		startNode(writer, "metadataPrefix", format.getMetadataPrefix());
		startNode(writer, "schema", format.getSchema());
		startNode(writer, "metadataNamespace", format.getMetadataNamespace());
	}

	@SuppressWarnings("nls")
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		MetadataFormat format = new MetadataFormat();
		for( ; reader.hasMoreChildren(); reader.moveUp() )
		{
			reader.moveDown();
			String name = reader.getNodeName();
			String value = reader.getValue();
			if( name.equals("oai:metadataPrefix") || name.equals("metadataPrefix") )
			{
				format.setMetadataPrefix(value);
			}
			else if( name.equals("oai:schema") || name.equals("schema") )
			{
				format.setSchema(value);
			}
			else if( name.equals("oai:metadataNamespace") || name.equals("metadataNamespace") )
			{
				format.setMetadataNamespace(value);
			}
		}

		return format;
	}

	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(MetadataFormat.class);
	}

}
