/*
 * Created on May 25, 2005
 */
package com.dytech.common.xml.mapping;

import java.io.Serializable;

import com.dytech.common.xml.ReflectionProvider;
import com.dytech.common.xml.XMLPath;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 */
public abstract class AbstractMapping implements Comparable<AbstractMapping>, Serializable
{
	private static final long serialVersionUID = 1;

	private static final ReflectionProvider REFLECTION = new ReflectionProvider();

	protected XMLPath nodePath;
	protected final String name;

	public AbstractMapping(String name, String node)
	{
		this.name = name;
		setXpath(node);
	}

	protected void setXpath(String xpath)
	{
		nodePath = new XMLPath(xpath);
	}

	public abstract void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object);

	public void unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context, Object object)
	{
		Object value = getUnmarshalledValue(object, reader, context);
		write(object, value);
	}

	protected void write(Object object, Object value)
	{
		if( !nodePath.hasAttribute() || value != null )
		{
			REFLECTION.writeField(object, name, value);
		}
	}

	protected boolean isBlank(String value)
	{
		return value != null && value.length() == 0;
	}

	protected abstract Object getUnmarshalledValue(Object object, HierarchicalStreamReader reader,
		UnmarshallingContext context);

	public abstract boolean hasValue(Object object);

	protected Object getField(Object object)
	{
		return REFLECTION.getField(object, name);
	}

	protected void writeField(Object object, Object value)
	{
		REFLECTION.writeField(object, name, value);
	}

	protected Object newInstance(Class<?> clazz)
	{
		return REFLECTION.newInstance(clazz);
	}

	public XMLPath getNodePath()
	{
		return nodePath;
	}

	public String getFieldName()
	{
		return name;
	}

	@Override
	@SuppressWarnings("nls")
	public String toString()
	{
		return "{" + name + ":" + nodePath + "}";
	}

	@Override
	public int compareTo(AbstractMapping m1)
	{
		int compare = -1;
		if( m1.getNodePath().hasAttribute() )
		{
			compare = 1;
		}
		return compare;
	}
}
