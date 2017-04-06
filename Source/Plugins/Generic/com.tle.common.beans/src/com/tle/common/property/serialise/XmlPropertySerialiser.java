/*
 * Created on 6/12/2005
 */
package com.tle.common.property.serialise;

import java.lang.reflect.Field;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.tle.common.property.annotation.PropertyBag;

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
