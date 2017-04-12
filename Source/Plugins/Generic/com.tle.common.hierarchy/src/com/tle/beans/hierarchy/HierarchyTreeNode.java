package com.tle.beans.hierarchy;

import com.tle.common.LazyTreeNode;

/**
 * @author Nicholas Read
 */
public class HierarchyTreeNode extends LazyTreeNode
{
	private static final long serialVersionUID = 1L;

	private long id;
	private boolean grantedEditTopic;

	public HierarchyTreeNode()
	{
		super();
	}

	public HierarchyTreeNode(long id)
	{
		this.id = id;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public boolean isGrantedEditTopic()
	{
		return grantedEditTopic;
	}

	public void setGrantedEditTopic(boolean grantedEditTopic)
	{
		this.grantedEditTopic = grantedEditTopic;
	}
}
