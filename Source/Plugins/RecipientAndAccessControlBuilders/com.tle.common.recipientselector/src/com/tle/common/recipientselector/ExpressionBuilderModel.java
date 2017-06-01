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

import java.util.Enumeration;
import java.util.Stack;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.recipientselector.ExpressionTreeNode.Grouping;
import com.tle.common.security.expressions.PostfixExpressionParser;
import com.tle.common.security.expressions.PostfixExpressionParser.BooleanOp;

/**
 * @author Nicholas Read
 */
public class ExpressionBuilderModel extends DefaultTreeModel
{
	private static final long serialVersionUID = 1L;

	public ExpressionBuilderModel()
	{
		super(getDefaultRootNode(), true);
	}

	private static ExpressionTreeNode getDefaultRootNode()
	{
		return new ExpressionTreeNode(Grouping.MATCH_ANY);
	}

	public ExpressionTreeNode getExpressionRoot()
	{
		return (ExpressionTreeNode) getRoot();
	}

	public void setExpression(String expression)
	{
		if( Check.isEmpty(expression) )
		{
			setRoot(getDefaultRootNode());
		}
		else
		{
			Parser parser = new Parser();
			setRoot(parser.convertToTreeNodes(expression));
		}
	}

	public String getExpression()
	{
		ExpressionTreeNode node = cloneExpressionAndAddTemporaryNots(getExpressionRoot());
		node = removeUnnecessaryNodes(node);

		if( node == null )
		{
			throw new RuntimeApplicationException("No expression given.");
		}
		StringBuilder buffer = new StringBuilder();
		getExpression(buffer, node);
		return buffer.toString();
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		ExpressionTreeNode node = (ExpressionTreeNode) path.getLastPathComponent();
		node.setGrouping((Grouping) newValue);
		nodeChanged(node);
	}

	private void getExpression(StringBuilder buffer, ExpressionTreeNode node)
	{
		if( node == null )
		{
			return;
		}

		if( node.isGrouping() )
		{
			Grouping grouping = node.getGrouping();

			if( grouping == Grouping.TEMPORARY_NOT )
			{
				if( node.getChildCount() > 0 )
				{
					ExpressionTreeNode child = (ExpressionTreeNode) node.getChildAt(0);
					getExpression(buffer, child);

					buffer.append(BooleanOp.NOT_TOKEN.toString());
					buffer.append(' ');
				}
			}
			else if( grouping == Grouping.MATCH_ALL || grouping == Grouping.MATCH_ANY )
			{
				BooleanOp op = grouping == Grouping.MATCH_ALL ? BooleanOp.AND_TOKEN : BooleanOp.OR_TOKEN;

				final int childCount = node.getChildCount();
				for( int i = 0; i < childCount; i++ )
				{
					ExpressionTreeNode child = (ExpressionTreeNode) node.getChildAt(i);
					getExpression(buffer, child);

					if( i > 0 )
					{
						buffer.append(op.toString());
						buffer.append(' ');
					}
				}
			}
			else
			{
				throw new IllegalStateException("Unhandled Grouping type: " + grouping.name());
			}
		}
		else
		{
			buffer.append(node.getExpression());
			buffer.append(' ');
		}
	}

	private ExpressionTreeNode cloneExpressionAndAddTemporaryNots(ExpressionTreeNode original)
	{
		ExpressionTreeNode newNode = new ExpressionTreeNode(original.getGrouping());
		newNode.setExpression(original.getExpression());

		ExpressionTreeNode nodeToAddChildrenTo = newNode;
		if( newNode.getGrouping() == Grouping.MATCH_NONE )
		{
			nodeToAddChildrenTo = new ExpressionTreeNode(Grouping.MATCH_ANY);
			newNode.add(nodeToAddChildrenTo);
			newNode.setGrouping(Grouping.TEMPORARY_NOT);
		}

		for( Enumeration<?> e = original.children(); e.hasMoreElements(); )
		{
			nodeToAddChildrenTo.add(cloneExpressionAndAddTemporaryNots((ExpressionTreeNode) e.nextElement()));
		}

		return newNode;
	}

	private ExpressionTreeNode removeUnnecessaryNodes(ExpressionTreeNode root)
	{
		// Start by removing
		removeUnnecessaryChildNodesFromNode(root);

		final int childCount = root.getChildCount();
		if( childCount == 0 )
		{
			root = null;
		}
		else if( childCount == 1
			&& (root.getGrouping() == Grouping.MATCH_ALL || root.getGrouping() == Grouping.MATCH_ANY) )
		{
			root = (ExpressionTreeNode) root.getChildAt(0);
		}

		return root;
	}

	private void removeUnnecessaryChildNodesFromNode(ExpressionTreeNode node)
	{
		for( int i = node.getChildCount() - 1; i >= 0; i-- )
		{
			ExpressionTreeNode child = (ExpressionTreeNode) node.getChildAt(i);

			// Recurse first...
			removeUnnecessaryChildNodesFromNode(child);

			// Remove ANDs and ORs with zero or one children
			final Grouping childGroup = child.getGrouping();
			final int childChildCount = child.getChildCount();

			if( childGroup != null )
			{
				if( childChildCount == 0 )
				{
					node.remove(i);
				}
				else if( childChildCount == 1 && (childGroup == Grouping.MATCH_ALL || childGroup == Grouping.MATCH_ANY) )
				{
					node.remove(i);
					node.insert((ExpressionTreeNode) child.getChildAt(0), i);
				}
			}
		}
	}

	/**
	 * @author Nicholas Read
	 */
	protected static class Parser extends PostfixExpressionParser<ExpressionTreeNode>
	{
		public ExpressionTreeNode convertToTreeNodes(String expression)
		{
			ExpressionTreeNode root = getResult(expression);

			root = ensureRootIsGroup(root);
			removeTemporaryNots(root);

			return root;
		}

		@Override
		protected void doAndOperator(Stack<Pair<ExpressionTreeNode, Integer>> operands)
		{
			doBinaryOperator(operands, Grouping.MATCH_ALL);
		}

		@Override
		protected void doOrOperator(Stack<Pair<ExpressionTreeNode, Integer>> operands)
		{
			doBinaryOperator(operands, Grouping.MATCH_ANY);
		}

		protected void doBinaryOperator(Stack<Pair<ExpressionTreeNode, Integer>> operands, Grouping grouping)
		{
			ExpressionTreeNode second = operands.pop().getFirst();
			ExpressionTreeNode first = operands.pop().getFirst();

			ExpressionTreeNode parent = null;
			if( first.getGrouping() == grouping )
			{
				parent = first;
				parent.add(second);
			}
			else if( second.getGrouping() == grouping )
			{
				parent = second;
				parent.insert(first, 0);
			}
			else
			{
				parent = new ExpressionTreeNode(grouping);
				parent.add(first);
				parent.add(second);
			}

			operands.push(new Pair<ExpressionTreeNode, Integer>(parent, -1));
		}

		@Override
		protected void doNotOperator(Stack<Pair<ExpressionTreeNode, Integer>> operands)
		{
			ExpressionTreeNode node = new ExpressionTreeNode(Grouping.TEMPORARY_NOT);
			node.insert(operands.pop().getFirst(), 0);
			operands.push(new Pair<ExpressionTreeNode, Integer>(node, -1));
		}

		@Override
		protected ExpressionTreeNode processOperand(String token)
		{
			return new ExpressionTreeNode(token);
		}

		private ExpressionTreeNode ensureRootIsGroup(ExpressionTreeNode root)
		{
			if( !root.isGrouping() )
			{
				ExpressionTreeNode child = root;
				root = getDefaultRootNode();
				root.add(child);
			}
			return root;
		}

		private void removeTemporaryNots(ExpressionTreeNode node)
		{
			if( node.getGrouping() == Grouping.TEMPORARY_NOT )
			{
				ExpressionTreeNode child = (ExpressionTreeNode) node.getChildAt(0);

				node.setGrouping(Grouping.MATCH_NONE);

				if( child.getGrouping() == Grouping.MATCH_ANY )
				{
					// Remove child, but inherit the child's children
					node.remove(child);
					for( int i = child.getChildCount() - 1; i >= 0; i-- )
					{
						node.insert((ExpressionTreeNode) child.getChildAt(i), 0);
					}
				}
			}

			// Recurse
			for( Enumeration<?> e = node.children(); e.hasMoreElements(); )
			{
				removeTemporaryNots((ExpressionTreeNode) e.nextElement());
			}
		}
	}
}
