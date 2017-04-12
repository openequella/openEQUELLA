package com.dytech.edge.ejb.helpers.metadata.mappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.dytech.edge.ejb.helpers.metadata.mapping.Mapping;
import com.tle.beans.entity.itemdef.mapping.IMSMapping.MappingType;

/**
 * Abstract class for mappers.
 */
public abstract class Mapper extends HashMap<String, Collection<Mapping>>
{
	public Mapper()
	{
		super();
	}

	public String process(String data)
	{
		return data;
	}

	protected void setValue(String key, String value, MappingType type, boolean repeat)
	{
		Collection<Mapping> col = get(key);
		if( col == null )
		{
			col = new HashSet<Mapping>();
			put(key, col);
		}
		col.add(new Mapping(key, value, type, repeat));
	}
}
