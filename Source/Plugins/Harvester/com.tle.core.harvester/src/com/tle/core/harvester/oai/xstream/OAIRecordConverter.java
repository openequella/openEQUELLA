/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.harvester.oai.data.Header;
import com.tle.core.harvester.oai.data.Record;

/**
 * 
 */
public class OAIRecordConverter extends OAIAbstractConverter
{
	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(Record.class);
	}

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		Record record = (Record) object;

		marshal(record.getHeader(), writer);
		startNode(writer, "metadata", record.getMetadata());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Record record = new Record();
		for( ; reader.hasMoreChildren(); reader.moveUp() )
		{
			reader.moveDown();
			String name = reader.getNodeName();
			if( name.equals("metadata") )
			{
				reader.moveDown();
				Object object = new OAIDOMConverter().unmarshal(reader, context);
				record.setMetadata(object);
				reader.moveUp();
			}
			else if( name.equals("header") )
			{
				Header header = (Header) convert(name, context);
				record.setHeader(header);
			}
		}

		return record;
	}

}
