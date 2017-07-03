/*
 * Created on May 24, 2005
 */
package com.tle.core.xstream.mapping;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.xstream.XMLDataChild;
import com.tle.core.xstream.XMLDataResolver;

/**
 * 
 */
public class DataMapping extends AbstractMapping
{
	Class clazz;
	private XMLDataResolver resolver;

	public DataMapping(String name, String node, Class clazz)
	{
		this(name, node, clazz, null);
	}

	public DataMapping(String name, String node, Class clazz, XMLDataResolver resolver)
	{
		super(name, node);
		this.clazz = clazz;
		setClassResolver(resolver);
	}

	public void setClassResolver(XMLDataResolver resolver)
	{
		if( resolver == null )
		{
			resolver = new DefaultDataResolver();
		}
		this.resolver = resolver;
	}

	@Override
	public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
	{
		if( name.length() > 0 )
		{
			object = getField(object);
		}

		resolver.writeClass(writer, object);
		context.convertAnother(object);
	}

	@Override
	public boolean hasValue(Object object)
	{
		return getField(object) != null;
	}

	@Override
	protected Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		Class clazz2use = resolver.resolveClass(reader);
		if( clazz2use == null )
		{
			clazz2use = clazz;
		}
		Object current = null;

		// Collections Data test
		if( name.length() > 0 )
		{
			// Allows fields to be pre-initialised
			current = getField(object);
		}
		if( current == null )
		{
			current = object;
		}

		Object retobj = context.convertAnother(current, clazz2use);
		if( retobj instanceof XMLDataChild )
		{
			((XMLDataChild) retobj).setParentObject(object);
		}
		return retobj;
	}

	public class DefaultDataResolver implements XMLDataResolver
	{
		private static final String XCLASS = "xclass";

		@Override
		public Class resolveClass(HierarchicalStreamReader reader)
		{
			String xclass = reader.getAttribute(XCLASS);
			Class clazz2use = null;
			if( xclass != null )
			{
				try
				{
					clazz2use = Class.forName(xclass, false, Thread.currentThread().getContextClassLoader());
				}
				catch( ClassNotFoundException e )
				{
					throw new RuntimeException(e);
				}
			}
			return clazz2use;
		}

		@Override
		public void writeClass(HierarchicalStreamWriter writer, Object object)
		{
			if( object.getClass() != clazz )
			{
				writer.addAttribute(XCLASS, object.getClass().getName());
			}
		}
	}
}
