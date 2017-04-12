/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.xstream;

import java.util.Iterator;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.harvester.oai.data.Header;

/**
 * 
 */
public class OAIHeaderConverter extends OAIAbstractConverter
{

	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(Header.class);
	}

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		Header header = (Header) object;

		addAttribute(writer, "status", header.getStatus());

		writer.startNode("identifier");
		writer.setValue(header.getIdentifier());
		writer.endNode();
		writer.startNode("datestamp");
		writer.setValue(header.getDatestamp());
		writer.endNode();
		Iterator i = header.getSpecs().iterator();
		while( i.hasNext() )
		{
			writer.startNode("setSpec");
			writer.setValue(i.next().toString());
			writer.endNode();
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Header header = new Header();

		String status = reader.getAttribute("status");
		header.setStatus(status);

		for( ; reader.hasMoreChildren(); reader.moveUp() )
		{
			reader.moveDown();
			String name = reader.getNodeName();
			String value = reader.getValue();
			if( name.equals("identifier") )
			{
				header.setIdentifier(value);
			}
			else if( name.equals("datestamp") )
			{
				header.setDatestamp(value);
			}
			else if( name.equals("setSpec") )
			{
				header.addSpec(value);
			}
		}

		return header;
	}

}
