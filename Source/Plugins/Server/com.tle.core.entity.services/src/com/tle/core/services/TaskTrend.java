package com.tle.core.services;

public class TaskTrend
{
	private final long workflowItemId;
	private final long nameId;
	private final int waiting;
	private int trend;

	public TaskTrend(long workflowItemId, long nameId, int waiting)
	{
		this.workflowItemId = workflowItemId;
		this.nameId = nameId;
		this.waiting = waiting;
		this.trend = waiting;
	}

	public int getTrend()
	{
		return trend;
	}

	public void setTrend(int trend)
	{
		this.trend = trend;
	}

	public long getNameId()
	{
		return nameId;
	}

	public int getWaiting()
	{
		return waiting;
	}

	public long getWorkflowItemId()
	{
		return workflowItemId;
	}
}