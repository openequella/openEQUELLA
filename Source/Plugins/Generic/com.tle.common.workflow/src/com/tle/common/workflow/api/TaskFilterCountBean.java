package com.tle.common.workflow.api;

import com.tle.common.workflow.TaskFilterCount;

/**
 * Pretty much identical to the TaskFilterCount
 *
 * @author larry
 *
 */
public class TaskFilterCountBean
{
	private String id;
	private String name;
	private String href;
	private String parent;
	private int count;

	public TaskFilterCountBean(TaskFilterCount taskFilterCount)
	{
		this.id = taskFilterCount.getId();
		this.name = taskFilterCount.getName();
		this.href = taskFilterCount.getHref();
		this.parent = taskFilterCount.getParent();
		this.count = taskFilterCount.getCount();
	}

	public TaskFilterCountBean(String id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getParent()
	{
		return parent;
	}

	public void setParent(String parent)
	{
		this.parent = parent;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public String getHref()
	{
		return href;
	}

	public void setHref(String href)
	{
		this.href = href;
	}
}
