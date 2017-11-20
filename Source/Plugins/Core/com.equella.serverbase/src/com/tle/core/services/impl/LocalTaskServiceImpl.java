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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.tle.annotation.Nullable;
import com.tle.common.NamedThreadFactory;
import com.tle.core.services.GlobalTaskStartInfo;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.TaskStatusChange;
import com.tle.core.services.TaskStatusListener;
import com.tle.core.services.TimeoutException;

@SuppressWarnings("nls")
public class LocalTaskServiceImpl extends AbstractTaskServiceImpl
{
	private static final Log LOGGER = LogFactory.getLog(LocalTaskServiceImpl.class);

	private final Cache<String, TaskStatus> finishedStatuses = CacheBuilder.newBuilder()
		.expireAfterAccess(30, TimeUnit.MINUTES).build();

	private final Map<String, Task> runningTasks = new HashMap<String, Task>();

	/**
	 * All task statuses for anything that is running or suspended.
	 */
	private final Map<String, TaskStatusImpl> taskStatuses = new HashMap<String, TaskStatusImpl>();
	/**
	 * Only tasks actually running on a node somewhere.
	 */
	private final Map<String, ClusteredTask> activeTasks = new HashMap<String, ClusteredTask>();
	/**
	 * Mapping from globalId to taskId.
	 */
	private final Map<String, GlobalTaskStat> globalTasks = new HashMap<String, GlobalTaskStat>();

	private final Cache<String, BlockingQueue<SimpleMessage>> messageResponses = CacheBuilder.newBuilder()
		.expireAfterAccess(30, TimeUnit.MINUTES).build();

	private final ExecutorService listenerThread = Executors.newSingleThreadExecutor(new NamedThreadFactory(
		"TaskServiceImpl.listenerThread"));

	private final Object stateMutex = new Object();

	private Multimap<String, TaskStatusListener> taskListeners = Multimaps.synchronizedMultimap(ArrayListMultimap
		.<String, TaskStatusListener> create());
	private ExecutorService threadpool;
	private ExecutorService priorityThreadpool;
	private ExecutorService asyncPool;

	@PostConstruct
	public void init()
	{
		asyncPool = Executors.newFixedThreadPool(4);

		threadpool = createTaskExecutor();
		priorityThreadpool = createPriorityTaskExecutor();
	}

	private GlobalTaskStat getOrAddGlobal(String globalId)
	{
		GlobalTaskStat globalTaskStat = globalTasks.get(globalId);
		if( globalTaskStat == null )
		{
			globalTaskStat = new GlobalTaskStat();
			globalTasks.put(globalId, globalTaskStat);
		}
		return globalTaskStat;
	}

	@Override
	public GlobalTaskStartInfo getGlobalTask(ClusteredTask globalTask, long millis)
	{
		if( !globalTask.isGlobal() )
		{
			throw new Error("Must use startTask()");
		}

		final long end = System.currentTimeMillis() + millis;
		final String globalId = globalTask.getGlobalId();

		GlobalTaskStat globalTaskStat;
		ArrayBlockingQueue<String> queue;
		synchronized( stateMutex )
		{
			globalTaskStat = getOrAddGlobal(globalId);
			if( globalTaskStat.getTaskId() != null )
			{
				return new GlobalTaskStartInfo(globalTaskStat.getTaskId(), true);
			}
			queue = new ArrayBlockingQueue<String>(1);
			globalTaskStat.getWaiting().add(queue);
			sendStartTask(globalTask);
		}

		try
		{
			String taskId = queue.poll(end - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
			synchronized( stateMutex )
			{
				globalTaskStat.getWaiting().remove(queue);
			}
			if( taskId != null )
			{
				return new GlobalTaskStartInfo(taskId, false);
			}
			throw new RuntimeException("Failed to start global task:" + globalId);
		}
		catch( InterruptedException e )
		{
			throw Throwables.propagate(e);
		}
	}

	private String sendStartTask(ClusteredTask clusteredTask)
	{
		String uuid = UUID.randomUUID().toString();
		processStartTask(clusteredTask, uuid);
		return uuid;
	}

	@Override
	public String startTask(ClusteredTask clusteredTask)
	{
		if( clusteredTask.isGlobal() )
		{
			throw new Error("Must use getGlobalTask()");
		}
		return sendStartTask(clusteredTask);
	}

	@Override
	public Map<String, TaskStatus> getAllStatuses()
	{
		synchronized( stateMutex )
		{
			return Maps.<String, TaskStatus> newHashMap(taskStatuses);
		}
	}

	protected void processStartTask(final ClusteredTask clusteredTask, final String taskId)
	{
		asyncPool.submit(new AsyncTaskOperation()
		{
			@Override
			public void execute() throws Exception
			{
				if( clusteredTask.isGlobal() )
				{
					String globalId = clusteredTask.getGlobalId();
					GlobalTaskStat globalTaskStat = getOrAddGlobal(globalId);
					if( globalTaskStat.getTaskId() != null )
					{
						if( LOGGER.isDebugEnabled() )
						{
							LOGGER.debug("Global task already started " + globalId + ":" + taskId);
						}
						return;
					}
					globalTaskStat.setTaskId(taskId);
					List<BlockingQueue<String>> queues = globalTaskStat.getWaiting();
					for( BlockingQueue<String> queue : queues )
					{
						queue.offer(taskId);
					}
				}
				activeTasks.put(taskId, clusteredTask);
				taskStatuses.put(taskId, new TaskStatusImpl(null, clusteredTask.getInternalId()));
				final Task task = clusteredTask.createTask(getPluginService(), clusteredTask.getArgs());
				task.setTaskDetails(LocalTaskServiceImpl.this, taskId);
				runningTasks.put(taskId, task);

				Runnable newLocalTask = new LocalTask(task, clusteredTask);
				if( clusteredTask.isPriority() )
				{
					threadpool.submit(newLocalTask);
				}
				else
				{
					priorityThreadpool.submit(newLocalTask);
				}
				stateMutex.notifyAll();
				return;
			}
		});
	}

	private void taskErrored(Task task, Collection<TaskStatusChange<?>> statusChanges, Throwable t)
	{
		String taskId = task.getTaskId();
		LOGGER.error("Task returned error:", t);
		List<TaskStatusChange<?>> changes = Lists.newArrayList(statusChanges);
		changes.add(new FinishedStatusChange(t));
		synchronized( stateMutex )
		{
			runningTasks.remove(taskId);
		}
		processTaskUpdate(taskId, changes);
	}

	private void taskFinished(Task task, Collection<TaskStatusChange<?>> statusChanges)
	{
		String taskId = task.getTaskId();
		List<TaskStatusChange<?>> changes = Lists.newArrayList(statusChanges);
		changes.add(new FinishedStatusChange());
		synchronized( stateMutex )
		{
			runningTasks.remove(taskId);
		}
		processTaskUpdate(taskId, changes);
	}

	@Override
	public boolean haveTaskStatus(String taskId)
	{
		synchronized( stateMutex )
		{
			return taskStatuses.containsKey(taskId) || finishedStatuses.asMap().containsKey(taskId);
		}
	}

	@Nullable
	@Override
	public TaskStatus getTaskStatus(String taskId)
	{
		synchronized( stateMutex )
		{
			TaskStatus taskStatus = taskStatuses.get(taskId);
			if( taskStatus != null )
			{
				return taskStatus;
			}
			return finishedStatuses.getIfPresent(taskId);
		}
	}

	@Override
	public TaskStatus waitForTaskStatus(String taskId, long millis)
	{
		long started = System.currentTimeMillis();
		long end = started + millis;
		while( true )
		{
			synchronized( stateMutex )
			{
				TaskStatus taskStatus = getTaskStatus(taskId);
				if( taskStatus != null )
				{
					return taskStatus;
				}
				try
				{
					long now = System.currentTimeMillis();
					if( now < end )
					{
						stateMutex.wait(end - now);
					}
					else
					{
						return null;
					}
				}
				catch( InterruptedException e )
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void processMessage(String taskId, SimpleMessage message, boolean request)
	{
		synchronized( stateMutex )
		{
			if( request )
			{
				Task task = runningTasks.get(taskId);
				if( task != null )
				{
					task.postMessage(message);
				}
			}
			else
			{
				BlockingQueue<SimpleMessage> waitingQueue = messageResponses.getIfPresent(message.getMessageId());
				if( waitingQueue != null )
				{
					messageResponses.invalidate(message.getMessageId());
					waitingQueue.add(message);
				}
			}
		}
	}

	public void processAskUpdateMessage(final Collection<String> taskIds)
	{
		List<Task> tasksToPublish = Lists.newArrayList();
		synchronized( stateMutex )
		{
			for( String taskId : taskIds )
			{
				Task task = runningTasks.get(taskId);
				if( task != null )
				{
					tasksToPublish.add(task);
				}
			}
		}
		for( Task task : tasksToPublish )
		{
			task.publishStatus();
		}
	}

	protected void processTaskUpdate(final String taskId, final Collection<TaskStatusChange<?>> changes)
	{
		synchronized( stateMutex )
		{
			final TaskStatusImpl taskStatus = taskStatuses.get(taskId);
			if( taskStatus == null )
			{
				throw new RuntimeException("Unknown taskId:" + taskId);
			}
			ClusteredTask clusteredTask = activeTasks.get(taskId);
			synchronized( taskStatus )
			{
				for( TaskStatusChange<?> taskStatusChange : changes )
				{
					taskStatusChange.modifyStatus(taskStatus);
				}
			}
			Collection<TaskStatusListener> listeners = taskListeners.get(taskId);
			if( !listeners.isEmpty() )
			{
				final TaskStatusListener[] listenerArray = listeners.toArray(new TaskStatusListener[listeners.size()]);
				listenerThread.execute(new Runnable()
				{
					@Override
					public void run()
					{
						for( TaskStatusListener taskStatusListener : listenerArray )
						{
							taskStatusListener.taskStatusChanged(taskId, taskStatus);
						}
					}
				});
			}
			if( taskStatus.isFinished() )
			{
				activeTasks.remove(taskId);
				if( clusteredTask.getGlobalId() != null )
				{
					globalTasks.remove(clusteredTask.getGlobalId());
				}
				taskStatuses.remove(taskId);
				finishedStatuses.put(taskId, taskStatus);
				taskListeners.removeAll(taskId);
			}
		}
	}

	@Override
	public void updateTaskStatus(Task task, Collection<TaskStatusChange<?>> statusChanges, String appliesTo,
		String becomes)
	{
		processTaskUpdate(task.getTaskId(), statusChanges);
	}

	@Override
	public void askTaskChanges(Collection<String> taskIds)
	{
		if( taskIds.isEmpty() )
		{
			return;
		}
		processAskUpdateMessage(taskIds);
	}

	@Override
	public boolean isTaskActive(String taskId)
	{
		synchronized( stateMutex )
		{
			return activeTasks.containsKey(taskId);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T waitForTaskSubStatus(String taskId, String subStatus, long millis)
	{
		long started = System.currentTimeMillis();
		long end = started + millis;
		while( true )
		{
			synchronized( stateMutex )
			{
				TaskStatus taskStatus = getTaskStatus(taskId);
				Serializable subStatusObj = taskStatus != null ? (Serializable) taskStatus.getTaskSubStatus(subStatus)
					: null;
				if( subStatus != null )
				{
					return (T) subStatusObj;
				}
				try
				{
					long now = System.currentTimeMillis();
					if( now < end )
					{
						stateMutex.wait(end - now);
					}
					else
					{
						return null;
					}
				}
				catch( InterruptedException e )
				{
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public void postMessage(String taskId, Serializable message)
	{
		processMessage(taskId, new SimpleMessage(UUID.randomUUID().toString(), message), true);
	}

	@Override
	public void messageResponse(Task task, SimpleMessage message)
	{
		processMessage(task.getTaskId(), message, false);
	}

	@Override
	public <T> T postSynchronousMessage(String taskId, Serializable message, long millis)
	{
		String messageId = UUID.randomUUID().toString();
		SimpleMessage msg = new SimpleMessage(messageId, message);
		BlockingQueue<SimpleMessage> blockingQueue = new LinkedBlockingQueue<SimpleMessage>(1);
		messageResponses.put(messageId, blockingQueue);
		processMessage(taskId, msg, true);
		try
		{
			SimpleMessage poll = blockingQueue.poll(millis, TimeUnit.MILLISECONDS);
			if( poll != null )
			{
				return poll.getContents();
			}
			throw new TimeoutException("Timed out waiting for message response");
		}
		catch( InterruptedException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addTaskStatusListener(String taskId, TaskStatusListener listener)
	{
		taskListeners.put(taskId, listener);
	}

	@Override
	public String getRunningGlobalTask(String globalId)
	{
		synchronized( stateMutex )
		{
			GlobalTaskStat globalTaskStat = globalTasks.get(globalId);
			if( globalTaskStat != null )
			{
				return globalTaskStat.getTaskId();
			}
			return null;
		}
	}

	// STATE CLASSES /////////////////////////////////////////////////////////

	public static class GlobalTaskStat
	{
		private final List<BlockingQueue<String>> waiting = new ArrayList<BlockingQueue<String>>();

		private String taskId;

		public String getTaskId()
		{
			return taskId;
		}

		public void setTaskId(String taskId)
		{
			this.taskId = taskId;
		}

		public List<BlockingQueue<String>> getWaiting()
		{
			return waiting;
		}
	}

	public static class NodeStatus implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int hash;
		private final Set<String> tasks = new HashSet<String>();

		public NodeStatus(int hash)
		{
			this.hash = hash;
		}

		public void addTask(String taskId)
		{
			tasks.add(taskId);
		}

		public int getHash()
		{
			return hash;
		}

		public Set<String> getTasks()
		{
			return tasks;
		}

		@Override
		public String toString()
		{
			return "#" + hash;
		}
	}

	public class LocalTask extends Task implements Runnable
	{
		private final Task wrapped;
		private final ClusteredTask clusteredTask;

		protected LocalTask(Task wrapped, ClusteredTask clusteredTask)
		{
			this.wrapped = wrapped;
			this.clusteredTask = clusteredTask;
		}

		@Override
		public void run()
		{
			try
			{
				waitForDependencies(clusteredTask);
				wrapped.call();
				taskFinished(wrapped, wrapped.getCurrentChanges());
			}
			catch( Throwable t )
			{
				taskErrored(wrapped, wrapped.getCurrentChanges(), t);
			}
		}

		@Override
		public Void call() throws Exception
		{
			run();
			return null;
		}

		@Override
		protected String getTitleKey()
		{
			if( wrapped == null )
			{
				return null;
			}
			return wrapped.getTitleKey();
		}
	}

	public abstract class AsyncTaskOperation implements Runnable
	{
		@Override
		public void run()
		{
			synchronized( stateMutex )
			{
				try
				{
					execute();
				}
				catch( Exception e )
				{
					LOGGER.error("Error processing async task operation", e);
				}
			}

		}

		public abstract void execute() throws Exception;
	}
}
