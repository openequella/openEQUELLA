/*
 * Created on Jun 22, 2005
 */
package com.tle.core.xstream;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Allows for mapping from a specified attribute value to a class to resolve to
 * for DataMappings.
 * 
 * @author Charles O'Farrell
 */
public class XMLDataResolverMapping implements XMLDataResolver
{
	private final BiMap<String, Class<?>> mapping;
	private final String attribute;
	private final Class<?> defaultClass;

	public XMLDataResolverMapping(String attribute)
	{
		this(attribute, null);
	}

	public XMLDataResolverMapping(String attribute, Class<?> defaultClass)
	{
		this.attribute = attribute;
		this.defaultClass = defaultClass;
		mapping = HashBiMap.create();
	}

	public void addMapping(String value, Class<?> clazz)
	{
		mapping.put(value, clazz);
	}

	@Override
	public Class<?> resolveClass(HierarchicalStreamReader reader)
	{
		String name = reader.getAttribute(attribute);
		Class<?> clazz = mapping.get(name);
		if( clazz == null )
		{
			clazz = defaultClass;
		}
		return clazz;
	}

	@Override
	public void writeClass(HierarchicalStreamWriter writer, Object object)
	{
		Class<?> clazz = object.getClass();
		if( !clazz.equals(defaultClass) )
		{
			String name = mapping.inverse().get(clazz);
			if( name != null )
			{
				writer.addAttribute(attribute, name);
			}
		}
	}
}
