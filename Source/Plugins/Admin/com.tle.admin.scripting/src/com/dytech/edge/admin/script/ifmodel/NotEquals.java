package com.dytech.edge.admin.script.ifmodel;

public class NotEquals implements Equality
{
	@Override
	public String toScript()
	{
		return "!=";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>!=</b>";
	}
}
