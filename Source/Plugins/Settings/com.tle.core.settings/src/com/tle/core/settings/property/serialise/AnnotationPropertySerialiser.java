/*
 * Created on 6/12/2005
 */
package com.tle.core.settings.property.serialise;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public abstract class AnnotationPropertySerialiser<T extends Annotation> extends PropertySerialiser
{
	@Override
	public void query(Object object, Field field, Collection<String> queries) throws Exception
	{
		T t = field.getAnnotation(getType());
		if( t != null )
		{
			queries.add(getKey(t));
		}
	}

	@Override
	public void load(Object object, Field field, Map<String, String> properties) throws Exception
	{
		T t = field.getAnnotation(getType());
		if( t != null )
		{
			load(object, field, t, properties);
		}
	}

	@Override
	public void save(Object object, Field field, Map<String, String> properties) throws Exception
	{
		T t = field.getAnnotation(getType());
		if( t != null )
		{
			save(object, field, t, properties);
		}
	}

	abstract String getKey(T property);

	abstract Class<T> getType();

	abstract void load(Object object, Field field, T details, Map<String, String> properties) throws Exception;

	abstract void save(Object object, Field field, T details, Map<String, String> properties) throws Exception;
}
