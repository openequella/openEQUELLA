/*
 * Created on 6/12/2005
 */
package com.tle.common.property.serialise;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.tle.common.property.annotation.Property;

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
