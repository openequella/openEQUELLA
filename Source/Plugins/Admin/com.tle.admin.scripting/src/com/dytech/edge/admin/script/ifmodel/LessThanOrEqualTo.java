package com.dytech.edge.admin.script.ifmodel;

public class LessThanOrEqualTo implements Equality
{
	@Override
	public String toScript()
	{
		return "<=";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>&lt;=</b>";
	}
}
