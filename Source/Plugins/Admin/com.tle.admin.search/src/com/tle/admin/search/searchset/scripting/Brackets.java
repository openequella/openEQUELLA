package com.tle.admin.search.searchset.scripting;

import com.dytech.edge.admin.script.model.Clause;
import com.dytech.edge.admin.script.model.Term;

public class Brackets implements Term
{
	protected Clause clause;

	public Brackets()
	{
		super();
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
