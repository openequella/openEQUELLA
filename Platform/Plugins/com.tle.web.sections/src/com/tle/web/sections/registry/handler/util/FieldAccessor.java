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