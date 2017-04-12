/*
 * Created on Aug 17, 2005
 */
package com.tle.common.old.workflow.node;

import com.tle.beans.entity.LanguageBundle;

public class ParallelNode extends WorkflowTreeNode
{
	private static final long serialVersionUID = 1;

	public ParallelNode(LanguageBundle name)
	{
		super(name);
	}

	public ParallelNode()
	{
		super();
	}

	@Override
	protected int getDefaultType()
	{
		return WorkflowNode.PARALLEL_TYPE;
	}

	@Override
	public boolean canHaveSiblingRejectPoints()
	{
		return false;
	}
}
