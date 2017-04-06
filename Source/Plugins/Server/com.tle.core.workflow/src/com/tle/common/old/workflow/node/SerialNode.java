/*
 * Created on Aug 17, 2005
 */
package com.tle.common.old.workflow.node;

import com.tle.beans.entity.LanguageBundle;

public class SerialNode extends WorkflowTreeNode
{
	private static final long serialVersionUID = 1;

	public SerialNode(LanguageBundle name)
	{
		super(name);
	}

	public SerialNode()
	{
		super();
	}

	@Override
	protected int getDefaultType()
	{
		return WorkflowNode.SERIAL_TYPE;
	}

	@Override
	public boolean canHaveSiblingRejectPoints()
	{
		return true;
	}
}
