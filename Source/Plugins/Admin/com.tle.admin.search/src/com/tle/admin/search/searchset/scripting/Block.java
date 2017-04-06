package com.tle.admin.search.searchset.scripting;

import com.dytech.edge.admin.script.model.Clause;
import com.dytech.edge.admin.script.model.Node;

public class Block extends Node
{
	protected Clause clause;
	protected Node parent;

	public Clause getClause()
	{
		return clause;
	}

	public void setClause(Clause clause)
	{
		this.clause = clause;
	}

	public String toScript()
	{
		return clause.toScript();
	}

	public String toEasyRead()
	{
		return "<b>where</b> ";
	}
}
