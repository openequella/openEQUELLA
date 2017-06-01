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

package com.tle.core.metadata.scripting.types.impl;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.scripting.types.MapScriptType;
import com.tle.core.metadata.scripting.types.MetadataScriptType;
import com.tle.web.scripting.impl.MapScriptTypeImpl;

public class MetadataScriptTypeImpl implements MetadataScriptType
{
	private Map<String, Map<String, String>> mtdt;

	public MetadataScriptTypeImpl(Map<String, Map<String, String>> mtdt)
	{
		this.mtdt = mtdt;
	}

	@Override
	public List<String> getTypesAvailable()
	{
		return Lists.newArrayList(mtdt.keySet());
	}

	@Override
	public MapScriptType getAllForType(String type)
	{
		Map<String, String> map = mtdt.get(type);

		return Check.isEmpty(map) ? null : new MapScriptTypeImpl(map);
	}

	@Override
	public String get(String type, String key)
	{
		Map<String, String> map = mtdt.get(type);
		return Check.isEmpty(map) ? null : map.get(key);
	}

	@Override
	public String get(String key)
	{
		String value = null;
		for( Map<String, String> grouping : mtdt.values() )
		{
			value = grouping.get(key);
			if( value != null )
			{
				return value;
			}
		}
		return value;
	}

	@Override
	public boolean isEmpty()
	{
		return mtdt.isEmpty();
	}
}
