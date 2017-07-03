/*
 * Created on May 24, 2005
 */
package com.tle.core.xstream.mapping;

import java.util.List;

/**
 * 
 */
public class ListMapping extends CollectionMapping
{
	public ListMapping(String name, String node)
	{
		super(name, node);
	}

	public ListMapping(String name, String node, Class type)
	{
		super(name, node, type);
	}

	public ListMapping(String name, String node, Class type, Class eltype)
	{
		super(name, node, type, eltype);
	}

	public ListMapping(String name, String node, Class type, AbstractMapping converter)
	{
		super(name, node, type, converter);
	}

	@Override
	public Class getRequiredType()
	{
		return List.class;
	}
}
