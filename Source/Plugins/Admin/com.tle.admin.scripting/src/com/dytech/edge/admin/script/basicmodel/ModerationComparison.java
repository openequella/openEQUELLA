package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.Equality;
import com.tle.common.i18n.CurrentLocale;

public class ModerationComparison implements Comparison
{
	protected Equality op;
	protected boolean value;

	public ModerationComparison(Equality op, boolean value)
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

	public boolean getValue()
	{
		return value;
	}

	public void setValue(boolean value)
	{
		this.value = value;
	}

	@Override
	public String toScript()
	{
		return "moderationallowed " + op.toScript() + " " + value;
	}

	@Override
	public String toEasyRead()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.target.moderator") + " " + op.toEasyRead() + " " + value;
	}
}
