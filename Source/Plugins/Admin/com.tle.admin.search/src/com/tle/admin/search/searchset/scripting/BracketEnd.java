package com.tle.admin.search.searchset.scripting;

import com.dytech.edge.admin.script.model.Node;

public class BracketEnd extends Node
{
	protected Node node;

	public BracketEnd(Node node, String tab)
	{
		setText(tab + "<b>)</b>");
		this.node = node;
	}

	public Node getNode()
	{
		return node;
	}
}
