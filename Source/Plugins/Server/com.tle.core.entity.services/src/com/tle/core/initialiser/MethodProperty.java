package com.tle.core.initialiser;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class MethodProperty extends Property
{
	private final PropertyDescriptor descriptor;

	public MethodProperty(PropertyDescriptor descriptor)
	{
		this.descriptor = descriptor;
	}

	@Override
	public Class<?> getReturnType()
	{
		return getMethod().getReturnType();
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> name)
	{
		return getMethod().isAnnotationPresent(name);
	}

	@Override
	<T extends Annotation> T getAnnotation(Class<T> arg0)
	{
		return getMethod().getAnnotation(arg0);
	}

	private Method getMethod()
	{
		return descriptor.getReadMethod();
	}

	private Method setMethod()
	{
		return descriptor.getWriteMethod();
	}

	@Override
	public Object get(Object object)
	{
		try
		{
			return getMethod().invoke(object, new Object[]{});
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, Object o)
	{
		try
		{
			setMethod().invoke(object, new Object[]{o});
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}
}
