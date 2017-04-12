package com.dytech.edge.admin.script.ifmodel;

public class LessThan implements Equality
{
	@Override
	public String toScript()
	{
		return "<";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>&lt;</b>&nbsp;";
	}
}
