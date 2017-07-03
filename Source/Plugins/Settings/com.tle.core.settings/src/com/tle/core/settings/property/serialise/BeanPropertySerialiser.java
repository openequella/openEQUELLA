/*
 * Created on 6/12/2005
 */
package com.tle.core.settings.property.serialise;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.core.settings.property.PropertyBeanFactory;

public class BeanPropertySerialiser extends PropertySerialiser
{
	@Override
	public void query(Object object, Field field, Collection<String> queries)
		throws IllegalAccessException, InstantiationException
	{
		Class<?> type = field.getType();
		if( ConfigurationProperties.class.isAssignableFrom(type) )
		{
			ConfigurationProperties fobject = (ConfigurationProperties) field.get(object);
			if( fobject == null )
			{
				fobject = (ConfigurationProperties) type.newInstance();
				field.set(object, fobject);
			}
			queries.addAll(PropertyBeanFactory.getSelect(fobject));
		}
	}

	@Override
	public void load(Object object, Field field, Map<String, String> properties)
		throws IllegalAccessException, InstantiationException
	{
		Class<?> type = field.getType();
		if( ConfigurationProperties.class.isAssignableFrom(type) )
		{
			ConfigurationProperties fobject = (ConfigurationProperties) field.get(object);
			if( fobject == null )
			{
				fobject = (ConfigurationProperties) type.newInstance();
				field.set(object, fobject);
			}
			PropertyBeanFactory.load(fobject, properties);
		}
	}

	@Override
	public void save(Object object, Field field, Map<String, String> properties) throws IllegalAccessException
	{
		if( ConfigurationProperties.class.isAssignableFrom(field.getType()) )
		{
			ConfigurationProperties fobject = (ConfigurationProperties) field.get(object);
			PropertyBeanFactory.save(fobject, properties);
		}
	}
}
