/*
 * Created on Jun 21, 2005
 */
package com.tle.core.freetext.queries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jmaginnis
 */
public class QueryKey implements Serializable
{
	private static final long serialVersionUID = 1L;
	List<Object> parts = new ArrayList<Object>(3);

	public QueryKey()
	{
		// don't add anything
	}

	public QueryKey(Object type)
	{
		parts.add(type);
	}

	public void add(Object part1)
	{
		parts.add(part1);
	}

	public void add(Object part1, Object part2)
	{
		parts.add(part1);
		parts.add(part2);
	}

	public void add(Object part1, Object part2, Object part3)
	{
		parts.add(part1);
		parts.add(part2);
		parts.add(part3);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj instanceof QueryKey )
		{
			QueryKey key2 = (QueryKey) obj;
			return key2.parts.equals(parts);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return parts.hashCode();
	}

	public void clear()
	{
		parts.clear();
	}

	@Override
	public String toString()
	{
		return parts.toString();
	}

	public String toSQLString()
	{
		StringBuilder sbuf = new StringBuilder();
		for( Object object : parts )
		{
			sbuf.append(object);
		}
		return sbuf.toString();
	}
}
