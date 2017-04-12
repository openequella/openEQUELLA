package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.Equality;
import com.dytech.edge.admin.script.ifmodel.IfModel;
import com.tle.common.i18n.CurrentLocale;

public class StatusComparison implements Comparison
{
	protected Equality op;
	protected String value;

	public StatusComparison(Equality op, String value)
	{
		this.op = op;
		this.value = value;
	}

	public Equality getOperation()
	{
		return op;
	}

	public void setOp(Equality op)
	{
		this.op = op;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public String toScript()
	{
		return "status " + op.toScript() + " '" + IfModel.encode(value) + "'";
	}

	@Override
	public String toEasyRead()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.target.status") + " " + op.toEasyRead() + " '" + value
			+ "'";
	}
}
