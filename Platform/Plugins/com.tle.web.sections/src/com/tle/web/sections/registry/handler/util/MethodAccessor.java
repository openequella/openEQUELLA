/**
 *
 */
package com.tle.web.sections.registry.handler.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class MethodAccessor implements PropertyAccessor
{
	private final Method getter;
	private final Method setter;
	private final String name;

	public MethodAccessor(PropertyDescriptor desc)
	{
		this(desc.getReadMethod(), desc.getWriteMethod(), desc.getName());
	}

	public MethodAccessor(Method getter, Method setter, String name)
	{
		this.getter = getter;
		this.setter = setter;
		this.name = name;
	}

	@Override
	public Object read(Object obj) throws Exception
	{
		return getter.invoke(obj);
	}

	@Override
	public void write(Object obj, Object value) throws Exception
	{
		setter.invoke(obj, value);
	}

	@Override
	public Type getType()
	{
		if( getter != null )
		{
			return getter.getGenericReturnType();
		}
		return setter.getGenericParameterTypes()[0];
	}

	@Override
	public String getName()
	{
		return name;
	}
}