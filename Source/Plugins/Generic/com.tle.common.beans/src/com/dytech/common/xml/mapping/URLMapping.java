/*
 * Created on May 24, 2005
 */
package com.dytech.common.xml.mapping;

import java.net.MalformedURLException;
import java.net.URL;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * 
 */
public class URLMapping extends NodeMapping
{
	public URLMapping(String name, String node)
	{
		super(name, node);
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Object value = super.getUnmarshalledValue(object, reader, context);

		try
		{
			value = new URL(value.toString());
		}
		catch( MalformedURLException e )
		{
			value = null;
			// IGNORE
		}

		return value;
	}
}
