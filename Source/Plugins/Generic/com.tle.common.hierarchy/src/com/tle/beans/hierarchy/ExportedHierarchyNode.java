package com.tle.beans.hierarchy;

import java.util.List;

import com.tle.common.security.TargetList;

public class ExportedHierarchyNode
{
	private HierarchyTopic topic;
	private List<ExportedHierarchyNode> children;
	private TargetList targetList;

	public List<ExportedHierarchyNode> getChildren()
	{
		return children;
	}

	public void setChildren(List<ExportedHierarchyNode> children)
	{
		this.children = children;
	}

	public TargetList getTargetList()
	{
		return targetList;
	}

	public void setTargetList(TargetList targetList)
	{
		this.targetList = targetList;
	}

	public HierarchyTopic getTopic()
	{
		return topic;
	}

	public void setTopic(HierarchyTopic topic)
	{
		this.topic = topic;
	}

}
