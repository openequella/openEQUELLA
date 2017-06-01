/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
