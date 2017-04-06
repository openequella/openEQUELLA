/*
 * Created on Feb 17, 2005
 */
package com.dytech.edge.common.valuebean;

import java.util.Objects;

import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import com.tle.common.Format;

/**
 * @author adame
 */
@SuppressWarnings("nls")
public class DefaultGroupBean implements GroupBean, FieldEquality<DefaultGroupBean>
{
	private final String id;
	private final String name;

	/**
	 * Contruct a default GroupBean
	 */
	public DefaultGroupBean(String id, String name)
	{
		Check.checkNotNull("id", id);
		Check.checkNotNull("name", name);

		this.id = id;
		this.name = name;
	}

	/**
	 * @return the id
	 */
	@Override
	public String getUniqueID()
	{
		return id;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return Check.commonEquals(this, obj);
	}

	@Override
	public boolean checkFields(DefaultGroupBean rhs)
	{
		return Objects.equals(id, rhs.id);
	}

	@Override
	public String toString()
	{
		return Format.format(this);
	}
}
