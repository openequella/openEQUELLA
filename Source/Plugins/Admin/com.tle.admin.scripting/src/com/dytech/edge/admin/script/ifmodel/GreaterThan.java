package com.dytech.edge.admin.script.ifmodel;

public class GreaterThan implements Equality
{
	@Override
	public String toScript()
	{
		return ">";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>&gt;</b>&nbsp;";
	}
}
