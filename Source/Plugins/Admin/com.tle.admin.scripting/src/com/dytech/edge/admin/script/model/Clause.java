package com.dytech.edge.admin.script.model;

import java.util.ArrayList;
import java.util.List;

public class Clause
{
	protected Term first;
	protected List<OpTerm> opterms;

	public Clause()
	{
		first = null;
		opterms = new ArrayList<OpTerm>();
	}

	public void setFirst(Term term)
	{
		first = term;
	}

	public void add(OpTerm opterm)
	{
		opterms.add(opterm);
		opterm.setParent(this);
	}

	public void remove(OpTerm opterm)
	{
		opterms.remove(opterm);
	}

	public Term getFirst()
	{
		return first;
	}

	public void insert(OpTerm opterm, int index)
	{
		if( index >= opterms.size() )
		{
			add(opterm);
		}
		else
		{
			opterms.add(index, opterm);
			opterm.setParent(this);
		}
	}

	public int indexOf(OpTerm opterm)
	{
		return opterms.indexOf(opterm);
	}

	public List<OpTerm> getOpTerms()
	{
		return opterms;
	}

	public String toScript()
	{
		StringBuilder script = new StringBuilder();

		script.append(first.toScript());
		script.append(" ");

		for( OpTerm opterm : opterms )
		{
			script.append(opterm.getOperator().toScript());
			script.append(" ");
			script.append(opterm.getTerm().toScript());
			script.append(" ");
		}

		return script.toString();
	}
}
