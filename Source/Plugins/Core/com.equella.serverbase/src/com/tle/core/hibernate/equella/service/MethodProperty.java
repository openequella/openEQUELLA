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
