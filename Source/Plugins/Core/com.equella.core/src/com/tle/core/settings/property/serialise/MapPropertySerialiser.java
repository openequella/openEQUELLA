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

import com.tle.common.settings.annotation.PropertyHashMap;

public class MapPropertySerialiser extends AnnotationPropertySerialiser<PropertyHashMap>
{
	@Override
	Class<PropertyHashMap> getType()
	{
		return PropertyHashMap.class;
	}

	@Override
	String getKey(PropertyHashMap property)
	{
		return property.key();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(Object object, Field field, PropertyHashMap property, Map<String, String> properties)
		throws IllegalAccessException, InstantiationException
	{
		String pkey = getKey(property) + '.';

		Map<String, String> map = (Map<String, String>) field.get(object);
		for( Map.Entry<String, String> entry : properties.entrySet() )
		{
			String key = entry.getKey();
			if( key.startsWith(pkey) )
			{
				String nkey = key.substring(pkey.length());
				map.put(nkey, entry.getValue());
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void save(Object object, Field field, PropertyHashMap property, Map<String, String> properties)
		throws IllegalAccessException
	{
		Object value = field.get(object);
		if( value != null )
		{
			Map<String, String> map = (Map<String, String>) value;
			String key = getKey(property) + '.';
			for( Map.Entry<String, String> entry : map.entrySet() )
			{
				properties.put(key + entry.getKey(), entry.getValue());
			}
		}
	}
}
