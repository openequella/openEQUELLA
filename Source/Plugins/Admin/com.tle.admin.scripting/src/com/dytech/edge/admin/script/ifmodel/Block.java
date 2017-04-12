package com.dytech.edge.admin.script.ifmodel;

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

	public String toScript(boolean first)
	{
		StringBuilder script = new StringBuilder();

		// We have to add the literals first.
		if( !first )
		{
			script.append("else ");
		}

		script.append("if( ");

		script.append(clause.toScript());

		// Add the remaining literals
		script.append(") \n{ \n    bRet = true; \n} \n");

		return script.toString();
	}

	public String toEasyRead(boolean first)
	{
		if( first )
		{
			return "<b>if</b> ";
		}
		else
		{
			return "<b>else if</b> ";
		}
	}
}
