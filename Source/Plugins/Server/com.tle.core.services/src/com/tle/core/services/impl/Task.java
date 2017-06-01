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
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.TaskStatusChange;

public abstract class Task implements Callable<Void>
{
	/**
	 * Enum values should be specified from highest to lower priority since the
	 * natural enum ordering (ordinal value) is used. An example of INTERACTIVE
	 * priority is a bulk item change action kicked off by a user. NORMAL is for
	 * tasks that aren't interactive per se, but should happen before any
	 * background tasks, like the background search indexer for example.
	 * BACKGROUND is for proper background tasks that have no user interaction
	 * and the lowest possible priority, like scheduled tasks, etc..
	 */
	public enum Priority
	{
		INTERACTIVE, NORMAL, BACKGROUND
	}

	protected PrivateTaskService taskService;

	private String taskId;
	private BlockingQueue<SimpleMessage> messageQueue = new LinkedBlockingDeque<SimpleMessage>();
	private StandardStatusChange currentChanges = new StandardStatusChange();
	private boolean shutdown = false;
	private String statusVersion = null;

	final Map<Class<?>, TaskStatusChange<?>> changes = Maps.newHashMap();

	public Task()
	{
		synchronized( changes )
		{
			StandardStatusChange statusChange = ensureChange();
			statusChange.setTitleKey(getTitleKey());
		}
	}

	@SuppressWarnings("nls")
	private StandardStatusChange ensureChange()
	{
		if( !Thread.holdsLock(changes) )
		{
			throw new Error("Must hold lock on changes");
		}
		StandardStatusChange taskStatusChange = (StandardStatusChange) changes.get(StandardStatusChange.class);
		if( taskStatusChange == null )
		{
			changes.put(StandardStatusChange.class, currentChanges);
		}
		return currentChanges;
	}

	public void setTaskDetails(PrivateTaskService service, String taskId)
	{
		this.taskService = service;
		this.taskId = taskId;
	}

	public String getTaskId()
	{
		return taskId;
	}

	public void addLogEntry(Serializable o)
	{
		synchronized( changes )
		{
			StandardStatusChange statusChange = ensureChange();
			statusChange.addLog(o);
		}
	}

	public void setSubTaskStatus(String name, Serializable o)
	{
		synchronized( changes )
		{
			StandardStatusChange statusChange = ensureChange();
			statusChange.putStatus(name, o);
		}
	}

	public Collection<TaskStatusChange<?>> getCurrentChanges()
	{
		return changes.values();
	}

	public void publishStatus()
	{
		synchronized( changes )
		{
			if( !changes.isEmpty() )
			{
				String becomes = UUID.randomUUID().toString();
				taskService.updateTaskStatus(this, Lists.newArrayList(changes.values()), statusVersion, becomes);
				this.statusVersion = becomes;
				changes.clear();
				currentChanges.reset();
			}
		}
	}

	public String getStatusVersion()
	{
		return statusVersion;
	}

	@SuppressWarnings("unchecked")
	public <T extends TaskStatusChange<T>> void mergeChanges(T change)
	{
		synchronized( changes )
		{
			T existing = (T) changes.get(change.getClass());
			if( existing != null )
			{
				existing.merge(change);
			}
			else
			{
				changes.put(change.getClass(), change);
			}
		}
	}

	protected void setupStatus(String statusKey, int maxWork)
	{
		synchronized( changes )
		{
			StandardStatusChange statusChange = ensureChange();
			statusChange.setStatusKey(statusKey);
			statusChange.setDoneWork(0);
			statusChange.setMaxWork(maxWork);
			publishStatus();
		}
	}

	protected void incrementWork()
	{
		synchronized( changes )
		{
			StandardStatusChange statusChange = ensureChange();
			statusChange.setDoneWork(statusChange.getDoneWork() + 1);
			publishStatus();
		}
	}

	public Priority getPriority()
	{
		return Priority.NORMAL;
	}

	protected abstract String getTitleKey();

	public void postMessage(SimpleMessage message)
	{
		messageQueue.add(message);
	}

	public void sendResponse(String messageId, Serializable message)
	{
		taskService.messageResponse(this, new SimpleMessage(messageId, message));
	}

	public SimpleMessage waitForMessage() throws InterruptedException
	{
		return messageQueue.take();

	}

	public SimpleMessage waitForMessage(long timeout, TimeUnit timeUnit) throws InterruptedException
	{
		return messageQueue.poll(timeout, timeUnit);
	}

	public void setFailoverStatus(TaskStatus status)
	{
		// nothing
	}

	protected boolean isShutdown()
	{
		return shutdown;
	}

	public void setShutdown(boolean shutdown)
	{
		this.shutdown = shutdown;
	}

	public void setUpdateVersion(String versionString)
	{
		this.statusVersion = versionString;
	}
}