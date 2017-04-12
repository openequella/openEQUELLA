package com.tle.common.recipientselector;

import javax.swing.tree.DefaultMutableTreeNode;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class ExpressionTreeNode extends DefaultMutableTreeNode
{
	private static final long serialVersionUID = 1L;

	public enum Grouping
	{
		MATCH_ALL(CurrentLocale.get("com.tle.admin.recipients.expressiontreenode.all")), MATCH_ANY(CurrentLocale
			.get("com.tle.admin.recipients.expressiontreenode.any")), MATCH_NONE(CurrentLocale
			.get("com.tle.admin.recipients.expressiontreenode.none")), TEMPORARY_NOT(CurrentLocale
			.get("com.tle.admin.recipients.expressiontreenode.not"));

		private final String display;

		private Grouping(String display)
		{
			this.display = display;
		}

		@Override
		public String toString()
		{
			return display;
		}
	}

	private Grouping grouping;
	private String expression;

	public ExpressionTreeNode(Grouping operator)
	{
		this.grouping = operator;
	}

	public ExpressionTreeNode(String expression)
	{
		this.expression = expression;
	}

	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	public void setGrouping(Grouping operator)
	{
		this.grouping = operator;
	}

	public Grouping getGrouping()
	{
		return grouping;
	}

	public boolean isGrouping()
	{
		return grouping != null;
	}

	@Override
	public boolean getAllowsChildren()
	{
		return isGrouping();
	}
}
