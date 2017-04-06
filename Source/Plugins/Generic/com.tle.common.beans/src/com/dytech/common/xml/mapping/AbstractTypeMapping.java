/*
 * Created on May 24, 2005
 */
package com.dytech.common.xml.mapping;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 *
 */
public abstract class AbstractTypeMapping extends NodeMapping
{
	protected Class<?> type;

	public AbstractTypeMapping(String name, String node, Class<?> type)
	{
		super(name, node);
		setType(type);
	}

	public abstract Class<?> getRequiredType();

	public abstract Class<?> getDefaultType();

	@Override
	public abstract void unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context, Object object);

	protected void setType(Class<?> type)
	{
		if( type == null )
		{
			type = getDefaultType();
		}
		Class<?> required = getRequiredType();
		if( !required.isAssignableFrom(type) )
		{
			throw new ClassCastException("Class must be of type " + required); //$NON-NLS-1$
		}
		this.type = type;
	}

	protected Object getInstantiatedField(Object object)
	{
		Object value = getField(object);
		if( value == null )
		{
			value = newInstance(type);
			writeField(object, value);
		}
		return value;
	}
}
