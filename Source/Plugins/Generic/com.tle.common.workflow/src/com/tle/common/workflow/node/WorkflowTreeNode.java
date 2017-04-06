package com.tle.common.workflow.node;

import com.tle.beans.entity.LanguageBundle;

public abstract class WorkflowTreeNode extends WorkflowNode
{
	public WorkflowTreeNode(LanguageBundle name)
	{
		super(name);
	}

	public WorkflowTreeNode()
	{
		super();
	}

	@Override
	public boolean isLeafNode()
	{
		return false;
	}

	@Override
	public boolean canAddChildren()
	{
		return true;
	}
}
