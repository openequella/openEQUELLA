package com.tle.common.searching;

import com.tle.common.Pair;

public class Field extends Pair<String, String>
{
	private static final long serialVersionUID = 1L;

	public Field(String field, String value)
	{
		super(field, value);
	}

	public String getField()
	{
		return getFirst();
	}

	public String getValue()
	{
		return getSecond();
	}

	@Override
	public String toString()
	{
		return getField() + ':' + getValue();
	}
}
