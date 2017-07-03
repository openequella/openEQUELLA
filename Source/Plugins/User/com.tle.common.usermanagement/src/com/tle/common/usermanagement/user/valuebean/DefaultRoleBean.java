/*
 * Created on Feb 17, 2005
 */
package com.tle.common.usermanagement.user.valuebean;

import java.util.Objects;

import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import com.tle.common.Format;

/**
 * @author adame
 */
public class DefaultRoleBean implements RoleBean, FieldEquality<DefaultRoleBean>
{
	private static final long serialVersionUID = 1L;
	private final String id;
	private final String name;

	public DefaultRoleBean(String id, String name)
	{
		Check.checkNotNull(id);
		Check.checkNotNull(name);

		this.id = id;
		this.name = name;
	}

	@Override
	public String getUniqueID()
	{
		return id;
	}

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
	public boolean checkFields(DefaultRoleBean rhs)
	{
		return Objects.equals(id, rhs.id);
	}

	@Override
	public String toString()
	{
		return Format.format(this);
	}
}
