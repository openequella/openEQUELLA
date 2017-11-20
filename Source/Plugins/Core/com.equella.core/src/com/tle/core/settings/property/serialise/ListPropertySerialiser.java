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

import java.util.Map;

import com.tle.common.settings.annotation.PropertyList;

public class ListPropertySerialiser extends AbstractListPropertySerialiser<PropertyList, String, Object>
{
	@Override
	Class<PropertyList> getType()
	{
		return PropertyList.class;
	}

	@Override
	protected String getKey(PropertyList t)
	{
		return t.key();
	}

	@Override
	protected Class<? extends Object> getType(PropertyList t)
	{
		return t.type();
	}

	@Override
	protected void load(Map<String, String> list, Map<String, String> properties, String nkey, String num, String value)
	{
		list.put(nkey, value);
	}

	@Override
	protected Object initialise(Class<? extends Object> type, String val)
	{
		Object object;
		if( type.isAssignableFrom(Integer.TYPE) || type.isAssignableFrom(Integer.class) )
		{
			object = Integer.parseInt(val);
		}
		else if( type.isAssignableFrom(Boolean.TYPE) || type.isAssignableFrom(Boolean.class) )
		{
			object = Boolean.parseBoolean(val);
		}
		else if( type.isAssignableFrom(Long.TYPE) || type.isAssignableFrom(Long.class) )
		{
			object = Long.parseLong(val);
		}
		else
		{
			object = val;
		}
		return object;
	}

	@Override
	protected void save(String key, Object o, Map<String, String> properties)
	{
		properties.put(key, o.toString());

	}
}
