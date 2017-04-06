package com.dytech.edge.admin.script.ifmodel;

import com.dytech.edge.admin.script.model.Clause;
import com.dytech.edge.admin.script.model.Term;

public class Brackets implements Term
{
	protected Clause clause;

	public Brackets()
	{
		// We have nothing to do here.
	}

	public Clause getClause()
	{
		return clause;
	}

	public void setClause(Clause clause)
	{
		this.clause = clause;
	}

	@Override
	public String toScript()
	{
		return '(' + clause.toScript() + ')';
	}
}
