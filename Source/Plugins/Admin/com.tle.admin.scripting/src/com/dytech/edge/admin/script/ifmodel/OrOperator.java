package com.dytech.edge.admin.script.ifmodel;

import com.dytech.edge.admin.script.model.Operator;

public class OrOperator implements Operator
{
	@Override
	public String toScript()
	{
		return "||";
	}

	@Override
	public String toEasyRead()
	{
		return "<b>or</b> ";
	}
}
