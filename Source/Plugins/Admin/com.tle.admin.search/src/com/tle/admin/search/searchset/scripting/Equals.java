package com.tle.admin.search.searchset.scripting;

public class Equals implements Equality
{
	@Override
	public String toScript()
	{
		return "=";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>equals</b>";
	}
}
