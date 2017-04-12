package com.tle.beans.item;

public class ItemTask
{
	private final Item item;
	private final String taskId;

	public ItemTask(Item item, String taskId)
	{
		this.item = item;
		this.taskId = taskId;
	}

	public Item getItem()
	{
		return item;
	}

	public String getTaskId()
	{
		return taskId;
	}
}
