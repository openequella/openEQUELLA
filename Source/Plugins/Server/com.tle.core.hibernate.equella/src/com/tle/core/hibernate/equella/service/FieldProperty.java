package com.tle.core.hibernate.equella.service;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.common.DontUseMethod;

public class FieldProperty extends Property
{
	private final Field field;
	private Method readMethod;
	private Method writeMethod;

	public FieldProperty(Field field)
	{
		this.field = field;
	}

	public FieldProperty(Field field, PropertyDescriptor descriptor)
	{
		this.field = field;
		if( descriptor != null && !isAnnotationPresent(DontUseMethod.class)
			&& descriptor.getPropertyType().isAssignableFrom(field.getType()) )
		{
			readMethod = descriptor.getReadMethod();
			writeMethod = descriptor.getWriteMethod();
		}
	}

	@Override
	public Class<?> getReturnType()
	{
		return field.getType();
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> name)
	{
		return field.isAnnotationPresent(name);
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> name)
	{
		return field.getAnnotation(name);
	}

	@Override
	public Object get(Object object)
	{
		try
		{
			if( readMethod != null )
			{
				return readMethod.invoke(object, (Object[]) null);
			}
			field.setAccessible(true);
			return field.get(object);
		}
		catch( Exception ex )
		{
			throw getException(object, field, null, ex);
		}
	}

	@Override
	public void set(Object object, Object o)
	{
		try
		{
			if( writeMethod != null )
			{
				writeMethod.invoke(object, new Object[]{o});
				return;
			}
			field.setAccessible(true);
			field.set(object, o);
		}
		catch( Exception ex )
		{
			throw getException(object, field, o, ex);
		}
	}

	private RuntimeException getException(Object object, Field field, Object value, Exception cause)
	{
		StringBuilder msg = new StringBuilder();
		msg.append("ObjectType:");
		msg.append(object.getClass().getName());
		msg.append(" Field:");
		msg.append(field.getName());
		if( value != null )
		{
			msg.append(" ValueType:");
			msg.append(value.getClass().getName());
		}

		return new RuntimeApplicationException(msg.toString(), cause);
	}
}
