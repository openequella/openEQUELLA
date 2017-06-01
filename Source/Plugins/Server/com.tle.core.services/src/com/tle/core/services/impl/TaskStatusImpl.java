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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.common.Pair;
import com.tle.core.services.TaskStatus;

public class TaskStatusImpl implements TaskStatus, Serializable
{
	private static final int MAX_LOGS = 3000;
	private static final String KEY_LOG = "LOG"; //$NON-NLS-1$

	private static final long serialVersionUID = 1L;

	private final Map<String, Serializable> stateMap = Maps.newHashMap();
	private transient Map<String, Object> transientMap;
	private boolean local;
	private boolean finished;
	private String errorMessage;
	private String titleKey;
	private String statusKey;
	private int maxWork;
	private int doneWork;
	private int percentage;
	private String versionString;
	private String nodeId;
	private int logOffset;
	private String internalId;

	public TaskStatusImpl(String nodeId, String internalId)
	{
		this.nodeId = nodeId;
		this.internalId = internalId;
	}

	@Override
	public String getInternalId()
	{
		return internalId;
	}

	@Override
	public int getMaxWork()
	{
		return maxWork;
	}

	public void setMaxWork(int maxWork)
	{
		this.maxWork = maxWork;
	}

	@Override
	public int getDoneWork()
	{
		return doneWork;
	}

	public void setDoneWork(int doneWork)
	{
		this.doneWork = doneWork;
	}

	@Override
	public int getPercentage()
	{
		return percentage;
	}

	public void setPercentage(int percentage)
	{
		this.percentage = percentage;
	}

	public boolean isLocal()
	{
		return local;
	}

	public void setLocal(boolean local)
	{
		this.local = local;
	}

	@Override
	public String getTitleKey()
	{
		return titleKey;
	}

	public void setTitleKey(String titleKey)
	{
		this.titleKey = titleKey;
	}

	@Override
	public String getStatusKey()
	{
		return statusKey;
	}

	public void setStatusKey(String statusKey)
	{
		this.statusKey = statusKey;
	}

	@Override
	public boolean isFinished()
	{
		return finished;
	}

	public void setFinished(boolean finished)
	{
		this.finished = finished;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> T getTaskSubStatus(String key)
	{
		return (T) stateMap.get(key);
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public Map<String, Serializable> getStateMap()
	{
		return stateMap;
	}

	public synchronized Map<String, Object> getTransientMap()
	{
		if( transientMap == null )
		{
			transientMap = Maps.newHashMap();
		}
		return transientMap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> T consumeTransient(String key)
	{
		return (T) getTransientMap().remove(key);
	}

	public synchronized void addToTaskLog(List<Serializable> log)
	{
		@SuppressWarnings("unchecked")
		LinkedList<Serializable> existingLog = (LinkedList<Serializable>) stateMap.get(KEY_LOG);
		if( existingLog == null )
		{
			existingLog = Lists.newLinkedList();
			stateMap.put(KEY_LOG, existingLog);
		}
		Iterator<Serializable> iter = log.iterator();
		while( iter.hasNext() )
		{
			Serializable logMsg = iter.next();
			if( existingLog.size() >= MAX_LOGS )
			{
				logOffset++;
				existingLog.removeFirst();
			}
			existingLog.add(logMsg);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T extends Serializable> List<T> getTaskLog()
	{
		List<T> existingLog = (List<T>) stateMap.get(KEY_LOG);
		if( existingLog == null )
		{
			return Collections.emptyList();
		}
		return existingLog;

	}

	@Override
	public synchronized <T extends Serializable> Pair<Integer, List<T>> getTaskLog(int askedOffset, int max)
	{
		int offset = askedOffset - logOffset;
		List<T> taskLog = getTaskSubStatus(KEY_LOG);
		if( offset < 0 || taskLog == null || offset >= taskLog.size() )
		{
			return new Pair<Integer, List<T>>(logOffset, Collections.<T> emptyList());
		}
		max = Math.min(taskLog.size() - offset, max);
		return new Pair<Integer, List<T>>(askedOffset, Lists.newArrayList(taskLog.subList(offset, offset + max)));
	}

	public String getVersionString()
	{
		return versionString;
	}

	public void setVersionString(String versionString)
	{
		this.versionString = versionString;
	}

	@Override
	public String getNodeIdRunning()
	{
		return nodeId;
	}

	public void setNodeIdRunning(String nodeId)
	{
		this.nodeId = nodeId;
	}
}
