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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.dytech.common.text.NumberStringComparator;

public abstract class AbstractListPropertySerialiser<T extends Annotation, M, N>
	extends
		AnnotationPropertySerialiser<T>
{
	protected abstract Class<? extends N> getType(T t);

	protected abstract void load(Map<String, M> list, Map<String, String> properties, String nkey, String num,
		String value);

	protected abstract N initialise(Class<? extends N> type, M val) throws InstantiationException,
		IllegalAccessException;

	protected abstract void save(String key, N n, Map<String, String> properties);

	@SuppressWarnings("unchecked")
	@Override
	public void load(Object object, Field field, T property, Map<String, String> properties)
		throws IllegalAccessException, InstantiationException
	{
		String pkey = getKey(property) + '.';
		Collection<N> col = (Collection<N>) field.get(object);
		Class<? extends N> type = getType(property);
		load(col, pkey, type, properties);

	}

	public void load(Collection<N> col, String pkey, Class<? extends N> type, Map<String, String> properties)
		throws IllegalAccessException, InstantiationException
	{
		Map<String, M> list = new TreeMap<String, M>(new NumberStringComparator<String>());

		if( !pkey.endsWith(".") ) //$NON-NLS-1$
		{
			pkey += '.';
		}

		for( Map.Entry<String, String> entry : properties.entrySet() )
		{
			String key = entry.getKey();
			if( key.startsWith(pkey) )
			{
				String nkey = key.substring(pkey.length());
				int dot = nkey.indexOf('.');
				String num = nkey;
				if( dot > 0 )
				{
					num = nkey.substring(0, dot);
					nkey = nkey.substring(dot + 1);
				}

				load(list, properties, nkey, num, entry.getValue());
			}
		}

		if( !list.isEmpty() )
		{
			if( col != null )
			{
				col.clear();
				for( M value : list.values() )
				{
					N prop = initialise(type, value);
					col.add(prop);
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void save(Object object, Field field, T property, Map<String, String> properties)
		throws IllegalAccessException
	{
		Object value = field.get(object);
		if( value != null )
		{
			Collection<N> col = (Collection<N>) value;
			String key = getKey(property);
			save(col, key, properties);
		}
	}

	public void save(Collection<N> col, String key, Map<String, String> properties)
	{
		int i = 0;
		for( N n : col )
		{
			save(key + '.' + i, n, properties);
			i++;
		}
	}
}
