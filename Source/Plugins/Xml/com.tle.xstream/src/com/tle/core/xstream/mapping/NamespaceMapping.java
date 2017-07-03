/*
 * Created on May 24, 2005
 */
package com.tle.core.xstream.mapping;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * 
 */
public class NamespaceMapping extends NodeMapping
{
	private static final String XMLNS = "xmlns";

	public NamespaceMapping(String name, String node)
	{
		super(name, node);
	}

	@Override
	public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
	{
		Map<String, String> namespaces = (Map) getMarshalledValue(object);
		if( namespaces != null )
		{
			for( Entry<String, String> entry : namespaces.entrySet() )
			{
				writer.addAttribute(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Iterator i = reader.getAttributeNames();
		Map map = null;
		while( i.hasNext() )
		{
			String ns = (String) i.next();
			if( ns.startsWith(XMLNS) )
			{
				String value = reader.getAttribute(ns);
				if( map == null )
				{
					map = new HashMap();
				}
				map.put(ns, value);
			}

		}
		return map;
	}
}
