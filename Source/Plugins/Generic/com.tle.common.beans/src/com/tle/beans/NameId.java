package com.tle.beans;

import java.util.Objects;

import com.tle.common.Pair;

/**
 * @author Nicholas Read
 */
public class NameId extends Pair<String, Long>
{
	private static final long serialVersionUID = 1;

	public NameId()
	{
		super();
	}

	public NameId(String name, long id)
	{
		super(name, id);
	}

	public String getName()
	{
		return getFirst();
	}

	public void setName(String name)
	{
		setFirst(name);
	}

	public long getId()
	{
		return getSecond();
	}

	public void setId(long id)
	{
		setSecond(id);
	}

	@Override
	public boolean checkFields(Pair<String, Long> rhs)
	{
		// Only check the value of this object type
		return Objects.equals(rhs.getSecond(), getSecond());
	}
}
