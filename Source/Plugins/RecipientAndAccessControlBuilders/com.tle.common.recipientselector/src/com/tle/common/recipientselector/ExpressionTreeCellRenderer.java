package com.tle.common.recipientselector;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class ExpressionTreeCellRenderer extends DefaultTreeCellRenderer
{
	private static final long serialVersionUID = 1L;
	private ExpressionFormatter formatter;

	public ExpressionTreeCellRenderer(RemoteUserService userService)
	{
		formatter = new ExpressionFormatter(userService);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
		boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		ExpressionTreeNode node = (ExpressionTreeNode) value;
		if( node.isGrouping() )
		{
			setText(node.getGrouping().toString());
		}
		else
		{
			setText(formatter.convertToInfix(node.getExpression()));
		}

		return this;
	}
}
