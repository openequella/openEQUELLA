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

package com.tle.web.recipientselector.tree;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.i18n.CurrentLocale;

public class SelectionGroupingTreeNode extends SelectionTreeNode
{
	@SuppressWarnings("nls")
	public enum Grouping
	{
		MATCH_ALL(CurrentLocale.get("com.tle.web.recipientselector.expressiontreenode.all")), MATCH_ANY(CurrentLocale
			.get("com.tle.web.recipientselector.expressiontreenode.any")), MATCH_NONE(CurrentLocale
			.get("com.tle.web.recipientselector.expressiontreenode.none")), TEMPORARY_NOT(CurrentLocale
			.get("com.tle.web.recipientselector.expressiontreenode.not"));
		
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

	private String id;
	private boolean newGrouping;
	private List<SelectionExpressionTreeNode> selection = Lists.newArrayList();
	private List<SelectionGroupingTreeNode> children = Lists.newArrayList();
	private SelectionGroupingTreeNode parent;

	public SelectionGroupingTreeNode(Grouping grouping)
	{
		this.grouping = grouping;
	}

	public SelectionGroupingTreeNode getParent()
	{
		return parent;
	}

	public void setParent(SelectionGroupingTreeNode parent)
	{
		this.parent = parent;
	}

	public Grouping getGrouping()
	{
		return grouping;
	}

	public void setGrouping(Grouping grouping)
	{
		this.grouping = grouping;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public List<SelectionExpressionTreeNode> getSelection()
	{
		return selection;
	}

	public List<SelectionGroupingTreeNode> getChildren()
	{
		return children;
	}

	public void add(SelectionTreeNode node)
	{
		if( node instanceof SelectionExpressionTreeNode )
		{
			add((SelectionExpressionTreeNode) node);
		}
		else
		{
			add((SelectionGroupingTreeNode) node);
		}
	}

	public void add(SelectionExpressionTreeNode expression)
	{
		selection.add(expression);
	}

	public void add(SelectionGroupingTreeNode grouping)
	{
		children.add(grouping);
	}

	public void insertSelectionGroupingTree(List<SelectionGroupingTreeNode> nodes, int index)
	{
		children.addAll(index, nodes);
	}

	public void insert(SelectionTreeNode node, int index)
	{
		if( node instanceof SelectionExpressionTreeNode )
		{
			insert((SelectionExpressionTreeNode) node, index);
		}
		else
		{
			insert((SelectionGroupingTreeNode) node, index);
		}
	}

	public void insert(SelectionExpressionTreeNode node, int index)
	{
		selection.add(index, node);
	}

	public void insert(SelectionGroupingTreeNode node, int index)
	{
		children.add(index, node);
	}

	public SelectionGroupingTreeNode getChildAt(int index)
	{
		return children.get(index);
	}

	public boolean remove(SelectionGroupingTreeNode child)
	{
		return children.remove(child);
	}

	public boolean remove(SelectionExpressionTreeNode child)
	{
		return selection.remove(child);
	}

	public int getChildCount()
	{
		return children.size();
	}

	public boolean isNewGrouping()
	{
		return newGrouping;
	}

	public void setNewGrouping(boolean newGrouping)
	{
		this.newGrouping = newGrouping;
	}
}