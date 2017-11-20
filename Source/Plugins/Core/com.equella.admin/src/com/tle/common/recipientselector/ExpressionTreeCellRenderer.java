/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.recipientselector;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.tle.common.recipientselector.formatter.ExpressionFormatter;
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
