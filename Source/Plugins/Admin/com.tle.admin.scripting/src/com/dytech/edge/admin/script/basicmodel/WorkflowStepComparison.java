package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.Equality;
import com.dytech.edge.admin.script.ifmodel.IfModel;
import com.tle.common.i18n.CurrentLocale;

public class WorkflowStepComparison implements Comparison
{
	private final Equality op;
	private final String value;
	private final String name;

	public WorkflowStepComparison(Equality op, String value, String name)
	{
		this.op = op;
		this.value = value;
		this.name = name;
	}

	public Equality getOperation()
	{
		return op;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public String toScript()
	{
		return "workflowstep " + op.toScript() + " '" + IfModel.encode(value) + "'";
	}

	@Override
	public String toEasyRead()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.target.workflow") + " " + op.toEasyRead() + " '" + name
			+ "'";
	}
}
