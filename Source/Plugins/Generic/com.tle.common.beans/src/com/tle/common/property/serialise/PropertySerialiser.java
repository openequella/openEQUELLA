/*
 * Created on 6/12/2005
 */
package com.tle.common.property.serialise;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class PropertySerialiser
{
	protected static final Map<Class<? extends Object>, List<Field>> FIELD_CACHE = new HashMap<Class<? extends Object>, List<Field>>();

	public void save(Object object, Map<String, String> properties)
	{
		for( Field field : extractAllFields(object) )
		{
			field.setAccessible(true);

			try
			{
				save(object, field, properties);
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	public void load(Object object, Map<String, String> properties)
	{
		for( Field field : extractAllFields(object) )
		{
			field.setAccessible(true);

			try
			{
				load(object, field, properties);
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	public void query(Object object, Set<String> list)
	{
		for( Field field : extractAllFields(object) )
		{
			field.setAccessible(true);

			try
			{
				query(object, field, list);
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Recursively gets all the fields from superclasses.
	 */
	protected List<Field> extractAllFields(Object object)
	{
		List<Field> fields = new ArrayList<Field>();
		return extractAllFields(object.getClass(), fields);
	}

	/**
	 * Recursively gets all the fields from superclasses.
	 */
	protected List<Field> extractAllFields(Class<? extends Object> clazz, List<Field> fields)
	{
		if( FIELD_CACHE.containsKey(clazz) )
		{
			fields.addAll(FIELD_CACHE.get(clazz));
		}
		else
		{
			Field[] classFields = clazz.getDeclaredFields();
			fields.addAll(Arrays.asList(classFields));
			FIELD_CACHE.put(clazz, Arrays.asList(classFields));
		}

		Class<? extends Object> superClass = clazz.getSuperclass();
		if( superClass != null )
		{
			extractAllFields(superClass, fields);
		}
		return fields;
	}

	public abstract void load(Object object, Field field, Map<String, String> properties) throws Exception;

	public abstract void save(Object object, Field field, Map<String, String> properties) throws Exception;

	public abstract void query(Object object, Field field, Collection<String> queries) throws Exception;
}
