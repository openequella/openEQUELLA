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
