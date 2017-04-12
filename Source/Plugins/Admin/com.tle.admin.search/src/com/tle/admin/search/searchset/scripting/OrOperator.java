package com.tle.admin.search.searchset.scripting;

import com.dytech.edge.admin.script.model.Operator;

public class OrOperator implements Operator
{
	@Override
	public String toScript()
	{
		return "OR";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>or</b> ";
	}
}
