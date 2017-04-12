/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.harvester.oai.data.Set;

/**
 * 
 */
public class OAISetConverter extends OAIAbstractConverter
{

	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(Set.class);
	}

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		Set set = (Set) object;
		startNode(writer, "setSpec", set.getSpec());
		startNode(writer, "setName", set.getName());
		startNode(writer, "setDescription", set.getDescription());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Set set = new Set();
		for( ; reader.hasMoreChildren(); reader.moveUp() )
		{
			reader.moveDown();
			String name = reader.getNodeName();
			String value = reader.getValue();
			if( name.equals("setSpec") )
			{
				set.setSpec(value);
			}
			else if( name.equals("setName") )
			{
				set.setName(value);
			}
			else if( name.equals("setDescription") )
			{
				reader.moveDown();
				Object object = new OAIDOMConverter().unmarshal(reader, context);
				set.setDescription(object);
				reader.moveUp();
			}
		}

		return set;
	}

}
