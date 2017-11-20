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

package com.tle.core.xstream.mapping;

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
