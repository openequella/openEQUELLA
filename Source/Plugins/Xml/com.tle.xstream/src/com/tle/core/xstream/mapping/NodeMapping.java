/*
 * Created on May 24, 2005
 */
package com.tle.core.xstream.mapping;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * 
 */
public class NodeMapping extends AbstractMapping
{
	public NodeMapping(String name, String node)
	{
		super(name, node);
	}

	@Override
	public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
	{
		Object value = getMarshalledValue(object);
		if( value != null )
		{
			String string = value.toString();
			nodePath.setValue(writer, string);
		}
	}

	protected Object getMarshalledValue(Object object)
	{
		Object value = null;

		// Special case for collections
		if( name.length() > 0 )
		{
			value = getField(object);
		}
		else
		{
			value = object;
		}
		return value;
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		return nodePath.getValue(reader);
	}

	@Override
	public boolean hasValue(Object object)
	{
		return getMarshalledValue(object) != null;
	}
}
