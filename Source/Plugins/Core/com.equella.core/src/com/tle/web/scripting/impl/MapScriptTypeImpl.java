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

package com.tle.web.scripting.impl;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.tle.common.scripting.types.MapScriptType;

public class MapScriptTypeImpl implements MapScriptType
{
	private Map<?, ?> map;

	public MapScriptTypeImpl(Map<?, ?> map)
	{
		this.map = map;
	}

	@Override
	public Object get(Object key)
	{
		return map.get(key);
	}

	@Override
	public List<Object> listKeys()
	{
		return Lists.newArrayList(map.keySet());
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}
}
