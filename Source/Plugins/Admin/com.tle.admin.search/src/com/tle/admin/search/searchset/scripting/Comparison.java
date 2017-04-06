package com.tle.admin.search.searchset.scripting;

import com.dytech.edge.admin.script.model.Term;

public class Comparison implements Term
{
	protected String lhs;
	protected String rhs;
	protected Equality operator;

	public Comparison(Equality operator, String lhs, String rhs)
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
		return "/xml" + lhs + " " + operator.toScript() + " '" + WhereModel.encode(rhs) + "'";
	}

	public String toEasyRead()
	{
		return lhs + " " + operator.toEasyRead() + " '" + rhs + "'";
	}
}
