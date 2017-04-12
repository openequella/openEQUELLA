/*
 * Created on Jun 23, 2005
 */
package com.tle.beans.entity.itemdef.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class LiteralMapping implements Serializable
{
	private static final long serialVersionUID = 1;

	private String value;
	private Collection<Literal> literals = new ArrayList<Literal>();

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public Collection<Literal> getLiterals()
	{
		return literals;
	}
}
