package com.dytech.edge.admin.script.model;

public class OpTerm extends Node
{
	protected Operator operator;
	protected Term term;

	public OpTerm(Operator operator, Term term)
	{
		this.operator = operator;
		this.term = term;
	}

	public Term getTerm()
	{
		return term;
	}

	public Operator getOperator()
	{
		return operator;
	}

	public void setOperator(Operator operator)
	{
		this.operator = operator;
	}

	public void setTerm(Term term)
	{
		this.term = term;
	}
}
