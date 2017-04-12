package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.Equality;
import com.dytech.edge.admin.script.ifmodel.IfModel;

public class XpathComparison implements Comparison
{
	protected String lhs;
	protected String rhs;
	protected Equality operator;

	public XpathComparison(Equality operator, String lhs, String rhs)
	{
		setLHS(lhs);
		setRHS(rhs);
		setOperator(operator);
	}

	public String getLHS()
	{
		return lhs;
	}

	public void setLHS(String lhs)
	{
		this.lhs = lhs;
	}

	public String getRHS()
	{
		return rhs;
	}

	public void setRHS(String rhs)
	{
		this.rhs = rhs;
	}

	public Equality getOperator()
	{
		return operator;
	}

	public void setOperator(Equality operator)
	{
		this.operator = operator;
	}

	@Override
	public String toScript()
	{
		return "xml.get('" + lhs + "') " + operator.toScript() + " '" + IfModel.encode(rhs) + "'";
	}

	@Override
	public String toEasyRead()
	{
		return lhs + " " + operator.toEasyRead() + " '" + rhs + "'";
	}
}
