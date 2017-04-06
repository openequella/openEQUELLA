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
