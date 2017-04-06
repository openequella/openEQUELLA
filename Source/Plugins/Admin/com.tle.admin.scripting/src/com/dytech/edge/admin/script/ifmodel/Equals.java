package com.dytech.edge.admin.script.ifmodel;

public class Equals implements Equality
{
	public Equals()
	{
		// Nothing to see here, move along...
	}

	@Override
	public String toScript()
	{
		return "==";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>=</b>";
	}
}
