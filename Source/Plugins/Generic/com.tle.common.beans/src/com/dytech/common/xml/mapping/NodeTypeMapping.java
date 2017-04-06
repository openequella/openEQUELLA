/*
 * Created on May 24, 2005
 */
package com.dytech.common.xml.mapping;

import com.google.common.collect.BiMap;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * 
 */
public class NodeTypeMapping extends NodeMapping
{
	private BiMap<String, Integer> types;
	private Object defaultType;

	public NodeTypeMapping(String name, String node, BiMap<String, Integer> types)
	{
		this(name, node, types, null);
	}

	/**
	 * @param types - node value -> Object value. ie admin -> new Integer(5),
	 *            metadata -> new Integer(4), teacher -> new Integer(3),
	 */
	public NodeTypeMapping(String name, String node, BiMap<String, Integer> types, Object defaultType)
	{
		super(name, node);
		this.types = types;
		this.defaultType = defaultType;
	}

	@Override
	protected Object getMarshalledValue(Object object)
	{
		Object value = super.getMarshalledValue(object);

		if( value != null )
		{
			value = types.inverse().get(value);
		}
		else
		{
			value = types.inverse().get(defaultType);
		}
		return value;
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Object value = super.getUnmarshalledValue(object, reader, context);
		value = types.get(value);
		if( value == null )
		{
			value = defaultType;
		}
		return value;
	}
}
