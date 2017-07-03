/*
 * Created on Mar 3, 2005
 */

package com.tle.beans.ump;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;

public class RoleMapping implements ConfigurationProperties
{
	private static final long serialVersionUID = 1L;

	@Property(key = "id")
	private String id;
	@Property(key = "name")
	private String name;
	@Property(key = "expression")
	private String expression;

	public RoleMapping()
	{
		super();
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
