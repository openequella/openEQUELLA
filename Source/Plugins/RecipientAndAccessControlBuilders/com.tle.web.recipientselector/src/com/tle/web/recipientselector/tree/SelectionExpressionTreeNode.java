package com.tle.web.recipientselector.tree;

public class SelectionExpressionTreeNode extends SelectionTreeNode
{
	private final String expression;

	public SelectionExpressionTreeNode(String expression)
	{
		this.expression = expression;
	}

	public String getExpression()
	{
		return expression;
	}
}