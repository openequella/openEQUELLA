/*
 * Created on May 24, 2005
 */
package com.tle.core.xstream.mapping;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class SetMapping extends CollectionMapping
{
	public SetMapping(String name, String node)
	{
		super(name, node);
	}

	public SetMapping(String name, String node, Class<?> type)
	{
		super(name, node, type);
	}

	public SetMapping(String name, String node, Class<?> type, Class<?> eltype)
	{
		super(name, node, type, eltype);
	}

	public SetMapping(String name, String node, Class<?> type, AbstractMapping converter)
	{
		super(name, node, type, converter);
	}

	@Override
	public Class<?> getRequiredType()
	{
		return Set.class;
	}

	// Presumably the intent is to return the implementation class, so we
	// ignore Sonar's "loose coupling" warning
	@Override
	public Class<?> getDefaultType()
	{
		return HashSet.class; // NOSONAR
	}
}
