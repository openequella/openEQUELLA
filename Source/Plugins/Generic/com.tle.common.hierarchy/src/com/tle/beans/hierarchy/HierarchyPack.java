package com.tle.beans.hierarchy;

import java.io.Serializable;
import java.util.List;

import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.security.TargetList;

/**
 * @author Nicholas Read
 */
public class HierarchyPack implements Serializable
{
	private static final long serialVersionUID = 1L;

	private HierarchyTopic topic;
	private TargetList targetList;
	private List<ItemDefinition> inheritedItemDefinitions;
	private List<Schema> inheritedSchemas;

	public HierarchyPack()
	{
		super();
	}

	public List<ItemDefinition> getInheritedItemDefinitions()
	{
		return inheritedItemDefinitions;
	}

	public void setInheritedItemDefinitions(List<ItemDefinition> inheritedItemDefinitions)
	{
		this.inheritedItemDefinitions = inheritedItemDefinitions;
	}

	public List<Schema> getInheritedSchemas()
	{
		return inheritedSchemas;
	}

	public void setInheritedSchemas(List<Schema> inheritedSchemas)
	{
		this.inheritedSchemas = inheritedSchemas;
	}

	public HierarchyTopic getTopic()
	{
		return topic;
	}

	public void setTopic(HierarchyTopic topic)
	{
		this.topic = topic;
	}

	public TargetList getTargetList()
	{
		return targetList;
	}

	public void setTargetList(TargetList targetList)
	{
		this.targetList = targetList;
	}

}