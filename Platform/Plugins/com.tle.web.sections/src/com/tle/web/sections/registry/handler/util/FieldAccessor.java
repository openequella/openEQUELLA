/**
 * 
 */
package com.tle.web.sections.registry.handler.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class FieldAccessor implements PropertyAccessor
{
	private Field field;

	public FieldAccessor(Field field)
	{
		this.field = field;
		field.setAccessible(true);
	}

	@Override
	public Object read(Object obj) throws Exception
	{
		return field.get(obj);
	}

	@Override
	public String getName()
	{
		return field.getName();
	}

	@Override
	public void write(Object obj, Object value) throws Exception
	{
		field.set(obj, value);
	}

	@Override
	public Type getType()
	{
		return field.getGenericType();
	}
}