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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.tle.common.settings.annotation.Property;

public class BasicPropertySerialiser extends AnnotationPropertySerialiser<Property>
{
	@Override
	Class<Property> getType()
	{
		return Property.class;
	}

	@Override
	String getKey(Property property)
	{
		return property.key();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void load(Object object, Field field, Property property, Map<String, String> properties)
		throws IllegalAccessException, MalformedURLException
	{
		Class type = field.getType();
		String key = property.key();
		String value = properties.get(key);

		if( value != null )
		{
			if( String.class.isAssignableFrom(type) )
			{
				field.set(object, value);
			}
			else if( Integer.TYPE.isAssignableFrom(type) )
			{
				field.setInt(object, Integer.parseInt(value));
			}
			else if( Boolean.TYPE.isAssignableFrom(type) )
			{
				field.setBoolean(object, Boolean.parseBoolean(value));
			}
			else if( Long.TYPE.isAssignableFrom(type) )
			{
				field.setLong(object, Long.parseLong(value));
			}
			else if( URL.class.isAssignableFrom(type) )
			{
				field.set(object, new URL(value));
			}
			else if( Enum.class.isAssignableFrom(type) )
			{
				try
				{
					field.set(object, Enum.valueOf(type, value));
				}
				catch( IllegalArgumentException iae )
				{
					// Ignore if value does not match enum - do not set field
				}
			}
		}
	}

	@Override
	public void save(Object object, Field field, Property property, Map<String, String> properties)
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
