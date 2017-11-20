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

package com.tle.core.settings.property.serialise;

import java.lang.reflect.Field;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.tle.common.settings.annotation.PropertyBag;

public class XmlPropertySerialiser extends AnnotationPropertySerialiser<PropertyBag>
{
	@Override
	Class<PropertyBag> getType()
	{
		return PropertyBag.class;
	}

	@Override
	String getKey(PropertyBag property)
	{
		return property.key();
	}

	@Override
	public void load(Object object, Field field, PropertyBag property, Map<String, String> properties)
		throws IllegalAccessException
	{
		String key = property.key();
		String value = properties.get(key);

		if( value != null )
		{
			field.set(object, new PropBagEx(value));
		}
	}

	@Override
	public void save(Object object, Field field, PropertyBag property, Map<String, String> properties)
		throws IllegalAccessException
	{
		String key = property.key();
		Object value = field.get(object);
		if( value != null )
		{
			properties.put(key, value.toString());
		}
		else
		{
			properties.remove(key);
		}
	}
}
