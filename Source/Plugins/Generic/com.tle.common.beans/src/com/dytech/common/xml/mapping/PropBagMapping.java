/*
 * Created on Jun 2, 2005
 */
package com.dytech.common.xml.mapping;

import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * 
 */
public class PropBagMapping extends ElementMapping
{

	public PropBagMapping(String name, String node)
	{
		this(name, node, false);
	}

	public PropBagMapping(String name, String node, boolean useroot)
	{
		super(name, node, useroot);
	}

	@Override
	public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
	{
		PropBagEx node = (PropBagEx) getMarshalledValue(object);
		super.marshalNode(writer, context, node.getRootElement());
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Node element = (Node) super.getUnmarshalledValue(object, reader, context);
		PropBagEx xml = null;
		if( element != null )
		{
			xml = new PropBagEx(element);
		}
		return xml;
	}
}
