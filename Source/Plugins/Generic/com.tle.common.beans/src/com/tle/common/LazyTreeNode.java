package com.tle.common;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Nicholas Read
 */
public class LazyTreeNode extends DefaultMutableTreeNode
{
	private String name;
	private ChildrenState childrenState;

	public LazyTreeNode()
	{
		setAllowsChildren(true);
		setChildrenState(ChildrenState.UNLOADED);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ChildrenState getChildrenState()
	{
		return childrenState;
	}

	public void setChildrenState(ChildrenState childState)
	{
		this.childrenState = childState;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public static enum ChildrenState
	{
		UNLOADED, LOADING, LOADED
	}
}
