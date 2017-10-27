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

package com.tle.core.settings.property;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.beans.ConfigurationProperty;
import com.tle.common.settings.ConfigurationProperties;
import com.tle.core.settings.property.serialise.BasicPropertySerialiser;
import com.tle.core.settings.property.serialise.BeanPropertySerialiser;
import com.tle.core.settings.property.serialise.DataListPropertySerialiser;
import com.tle.core.settings.property.serialise.ListPropertySerialiser;
import com.tle.core.settings.property.serialise.MapPropertySerialiser;
import com.tle.core.settings.property.serialise.PropertySerialiser;
import com.tle.core.settings.property.serialise.XmlPropertySerialiser;

public final class PropertyBeanFactory implements Serializable
{
	private static final long serialVersionUID = 1L;

	static List<PropertySerialiser> serialisers = new ArrayList<PropertySerialiser>();

	static
	{
		serialisers.add(new BasicPropertySerialiser());
		serialisers.add(new DataListPropertySerialiser());
		serialisers.add(new ListPropertySerialiser());
		serialisers.add(new BeanPropertySerialiser());
		serialisers.add(new MapPropertySerialiser());
		serialisers.add(new XmlPropertySerialiser());
	}

	public static Map<String, String> fill(List<ConfigurationProperty> all)
	{
		Map<String, String> map = new HashMap<String, String>();
		for( ConfigurationProperty prop : all )
		{
			map.put(prop.getKey().getProperty(), prop.getValue());
		}
		return map;
	}

	public static void fill(List<ConfigurationProperty> all, ConfigurationProperties object)
	{
		load(object, fill(all));
	}

	public static void save(ConfigurationProperties object, Map<String, String> properties)
	{
		if( object == null )
		{
			return;
		}

		for( PropertySerialiser serialiser : serialisers )
		{
			try
			{
				serialiser.save(object, properties);
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void save(Collection object, String key, Map<String, String> properties)
	{
		try
		{
			if( object.size() > 0 )
			{
				if( object.iterator().next() instanceof ConfigurationProperties )
				{
					new DataListPropertySerialiser().save(object, key, properties);
				}
				else
				{
					new ListPropertySerialiser().save(object, key, properties);
				}
			}
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public static void load(ConfigurationProperties object, Map<String, String> properties)
	{
		if( object == null )
		{
			return;
		}
		if( properties.size() > 0 )
		{
			for( PropertySerialiser serialiser : serialisers )
			{
				try
				{
					serialiser.load(object, properties);
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void load(List object, String property, Map<String, String> map)
	{
		try
		{
			new ListPropertySerialiser().load(object, property, String.class, map);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static void load(List object, String property, Class type, Map<String, String> map)
	{
		try
		{
			new DataListPropertySerialiser().load(object, property, type, map);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public static Collection<String> getSelect(ConfigurationProperties object)
	{
		Set<String> list = new HashSet<String>();
		if( object != null )
		{
			for( PropertySerialiser serialiser : serialisers )
			{
				try
				{
					serialiser.query(object, list);
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
			}
		}

		return list;
	}

	private PropertyBeanFactory()
	{
		throw new Error();
	}
}
