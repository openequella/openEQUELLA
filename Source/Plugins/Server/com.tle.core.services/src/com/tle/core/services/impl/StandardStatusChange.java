package com.tle.core.services.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.core.services.TaskStatusChange;

public class StandardStatusChange implements TaskStatusChange<StandardStatusChange>
{
	private static final long serialVersionUID = 1L;

	private int doneWork;
	private int maxWork;
	private String titleKey;
	private String statusKey;
	private List<Serializable> log = Lists.newArrayList();
	private Map<String, Serializable> subStatuses = Maps.newHashMap();

	@Override
	public void modifyStatus(TaskStatusImpl taskStatus)
	{
		taskStatus.setDoneWork(doneWork);
		taskStatus.setMaxWork(maxWork);
		taskStatus.setTitleKey(titleKey);
		taskStatus.setStatusKey(statusKey);
		taskStatus.getStateMap().putAll(subStatuses);
		taskStatus.addToTaskLog(log);
		if( maxWork > 0 && doneWork > 0 )
		{
			taskStatus.setPercentage((int) (((float) doneWork) / maxWork * 100.0));
		}
		else
		{
			taskStatus.setPercentage(0);
		}
	}

	public StandardStatusChange()
	{
		// nothing
	}

	@SuppressWarnings("nls")
	@Override
	public void merge(StandardStatusChange newChanges)
	{
		throw new Error("Shouldn't ever be merged");
	}

	public void reset()
	{
		log.clear();
		subStatuses.clear();
	}

	public int getDoneWork()
	{
		return doneWork;
	}

	public void setDoneWork(int doneWork)
	{
		this.doneWork = doneWork;
	}

	public int getMaxWork()
	{
		return maxWork;
	}

	public void setMaxWork(int maxWork)
	{
		this.maxWork = maxWork;
	}

	public String getTitleKey()
	{
		return titleKey;
	}

	public void setTitleKey(String titleKey)
	{
		this.titleKey = titleKey;
	}

	public String getStatusKey()
	{
		return statusKey;
	}

	public void setStatusKey(String statusKey)
	{
		this.statusKey = statusKey;
	}

	public void addLog(Serializable o)
	{
		log.add(o);
	}

	public void putStatus(String name, Serializable o)
	{
		subStatuses.put(name, o);
	}
}
