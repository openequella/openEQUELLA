package com.dytech.edge.admin.script.ifmodel;

import com.dytech.edge.admin.script.model.Operator;

@SuppressWarnings("nls")
public class AndOperator implements Operator
{
	@Override
	public String toScript()
	{
		return "&&";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>and</b> ";
	}
}
