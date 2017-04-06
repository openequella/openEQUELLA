/*
 * Created on Apr 12, 2005
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
