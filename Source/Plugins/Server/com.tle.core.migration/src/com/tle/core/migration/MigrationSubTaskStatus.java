package com.tle.core.migration;

import java.io.Serializable;

public class MigrationSubTaskStatus implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String statusKey;
	private int currentDone;
	private int currentMax;
	private int done;
	private int max;

	public MigrationSubTaskStatus(String statusKey, int max)
	{
		this.max = max;
		this.currentMax = max;
		this.statusKey = statusKey;
	}

	public String getStatusKey()
	{
		return statusKey;
	}

	public void setStatusKey(String statusKey)
	{
		this.statusKey = statusKey;
	}

	public int getDone()
	{
		return done;
	}

	public void setDone(int done)
	{
		this.done = done;
	}

	public int getMax()
	{
		return max;
	}

	public void setMax(int max)
	{
		this.max = max;
	}

	public void increment()
	{
		this.done++;
		currentDone++;
	}

	public int getCurrentMax()
	{
		return currentMax;
	}

	public void setCurrentMax(int currentMax)
	{
		this.currentMax = currentMax;
	}

	public int getCurrentDone()
	{
		return currentDone;
	}

	public void setCurrentDone(int currentDone)
	{
		this.currentDone = currentDone;
	}
}
