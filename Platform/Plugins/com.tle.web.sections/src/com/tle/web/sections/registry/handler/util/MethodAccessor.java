/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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