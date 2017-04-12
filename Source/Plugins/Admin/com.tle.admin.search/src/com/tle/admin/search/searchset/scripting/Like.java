package com.tle.admin.search.searchset.scripting;

public class Like implements Equality
{
	@Override
	public String toScript()
	{
		return "LIKE";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>is like</b>";
	}
}
