/*
 * Created on Aug 17, 2005
 */
package com.tle.common.old.workflow.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.tle.beans.entity.LanguageBundle;

public abstract class WorkflowTreeNode extends WorkflowNode
{
	private static final long serialVersionUID = 1;

	private List<WorkflowNode> children;
	private boolean rejectPoint;

	public WorkflowTreeNode(LanguageBundle name)
	{
		super(name);
		children = new ArrayList<WorkflowNode>();
	}

	public WorkflowTreeNode()
	{
		this(null);
	}

	public WorkflowNode getChild(int index)
	{
		return children.get(index);
	}

	public void addChild(WorkflowNode child)
	{
		children.add(child);
	}

	public void addChild(int index, WorkflowNode child)
	{
		children.add(index, child);
	}

	public void removeChild(WorkflowNode child)
	{
		children.remove(child);
	}

	public int indexOfChild(WorkflowNode child)
	{
		return children.indexOf(child);
	}

	public Iterator<WorkflowNode> iterateChildren()
	{
		return children.iterator();
	}

	public int numberOfChildren()
	{
		return children.size();
	}

	@Override
	public boolean canAddChildren()
	{
		return true;
	}

	@Override
	protected int getDefaultType()
	{
		return WorkflowNode.ROOT_TYPE;
	}

	@Override
	public boolean isLeafNode()
	{
		return false;
	}

	public boolean isRejectPoint()
	{
		return rejectPoint;
	}

	public void setRejectPoint(boolean rejectPoint)
	{
		this.rejectPoint = rejectPoint;
	}

	public abstract boolean canHaveSiblingRejectPoints();
}
