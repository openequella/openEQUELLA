package com.tle.admin.search.searchset.scripting;

import com.dytech.edge.admin.script.model.Operator;

@SuppressWarnings("nls")
public class AndOperator implements Operator
{
	@Override
	public String toScript()
	{
		return "AND";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>and</b> ";
	}
}
