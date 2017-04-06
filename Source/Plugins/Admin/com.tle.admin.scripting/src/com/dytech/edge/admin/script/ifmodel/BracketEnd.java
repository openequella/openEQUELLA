package com.dytech.edge.admin.script.ifmodel;

import com.dytech.edge.admin.script.model.Node;

public class BracketEnd extends Fake
{
	protected Node node;

	public BracketEnd(Node node, String tab)
	{
		super(tab + "<b>)</b>");
		this.node = node;
	}

	public Node getNode()
	{
		return node;
	}
}
