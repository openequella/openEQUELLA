/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
