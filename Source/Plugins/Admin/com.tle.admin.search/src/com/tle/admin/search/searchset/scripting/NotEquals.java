package com.tle.admin.search.searchset.scripting;

public class NotEquals implements Equality
{
	@Override
	public String toScript()
	{
		return "<>";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>is not</b>";
	}
}
