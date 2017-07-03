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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.Reaper;
import org.apache.curator.framework.recipes.locks.Reaper.Mode;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dytech.edge.common.Version;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.NamedThreadFactory;
import com.tle.core.cluster.ClusterMessageHandler;
import com.tle.core.cluster.service.ClusterMessagingService;
import com.tle.core.plugins.PluginAwareObjectInputStream;
import com.tle.core.plugins.PluginAwareObjectOutputStream;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.services.GlobalTaskStartInfo;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.TaskStatusChange;
import com.tle.core.services.TaskStatusListener;
import com.tle.core.services.TimeoutException;
import com.tle.core.services.impl.ClusteredTaskServiceImpl.TaskRunnerHandler.TaskRunner;
import com.tle.core.zookeeper.ZookeeperService;

@NonNullByDefault
@SuppressWarnings("nls")
public class ClusteredTaskServiceImpl extends AbstractTaskServiceImpl
	implements
		PathChildrenCacheListener,
		ClusterMessageHandler
{
	private String ZK_TASKPATH = "tasks";
	private String ZK_GLOBALTASKPATH = "tasks-global";
	private String ZK_TASKOWNERPATH = "tasks-owner";

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusteredTaskServiceImpl.class);

	@Inject
	private ZookeeperService zookeeperService;
	@Inject
	private ClusterMessagingService clusterMessagingService;

	private CuratorFramework curator;
	private TaskStatusHandler statusHandler = new TaskStatusHandler();
	private TaskRunnerHandler runnerHandler = new TaskRunnerHandler();

	private PathChildrenCache globalCache;
	private PathChildrenCache taskCache;

	private final Cache<String, Serializable[]> taskArgs = CacheBuilder.newBuilder()
		.expireAfterWrite(5, TimeUnit.MINUTES).build();
	private Multimap<String, TaskStatusListener> taskListeners = Multimaps
		.synchronizedMultimap(ArrayListMultimap.<String, TaskStatusListener>create());
	private final Cache<String, BlockingQueue<SimpleMessage>> messageResponses = CacheBuilder.newBuilder()
		.expireAfterAccess(30, TimeUnit.MINUTES).build();
	private CompletionService<TaskResult> taskRunnerExecutor;
	private CompletionService<TaskResult> priorityTaskRunnerExecutor;

	private String ourNodeId;
	private long suspendedSessionId;

	private Object initialTasksLock = new Object();
	private boolean startedInitialTasks;

	@PostConstruct
	public void init()
	{
		taskRunnerExecutor = new ExecutorCompletionService<TaskResult>(createTaskExecutor());
		priorityTaskRunnerExecutor = new ExecutorCompletionService<TaskResult>(createPriorityTaskExecutor());

		Version version = ApplicationVersion.get();
		String prefix;
		if( version.isDevelopment() )
		{
			prefix = "tasksdev/";
		}
		else
		{
			prefix = "tasks-" + version.getMmr() + '/';
		}
		ZK_TASKPATH = prefix + ZK_TASKPATH;
		ZK_GLOBALTASKPATH = prefix + ZK_GLOBALTASKPATH;
		ZK_TASKOWNERPATH = prefix + ZK_TASKOWNERPATH;
		globalCache = zookeeperService.createPathCache(ZK_GLOBALTASKPATH, true);
		taskCache = zookeeperService.createPathCache(ZK_TASKPATH, false, this, StartMode.POST_INITIALIZED_EVENT);
		curator = zookeeperService.getCurator();
		ourNodeId = zookeeperService.getNodeId();

		final Reaper reaper = new Reaper(curator, 10000);
		try
		{
			reaper.start();
		}
		catch( Exception e1 )
		{
			Throwables.propagate(e1);
		}
		new TaskWatchThread("Task Finisher listener", taskRunnerExecutor, reaper).start();
		new TaskWatchThread("Priority Task Finisher listener", priorityTaskRunnerExecutor, reaper).start();
	}

	@Override
	public String startTask(ClusteredTask clusteredTask)
	{
		if( clusteredTask.isGlobal() )
		{
			throw new Error("Is a global task");
		}
		String taskId = UUID.randomUUID().toString();
		return startInternal(clusteredTask, taskId);
	}

	protected Serializable[] getArgsForTask(ClusteredTaskWithArgs withArgs)
	{
		String argsId = withArgs.getArgsId();
		if( argsId != null )
		{
			String nodeId = withArgs.getNodeId();
			if( nodeId.equals(ourNodeId) )
			{
				Serializable[] args = taskArgs.getIfPresent(argsId);
				taskArgs.invalidate(argsId);
				return args;
			}
			else
			{
				BlockingQueue<SimpleMessage> mq = createResponseQueue(argsId);
				clusterMessagingService.postMessage(nodeId, new ArgsMessage(argsId, ourNodeId));
				ArgsMessage argsResponse = waitForResponse(5000, mq);
				return argsResponse.getArgs();
			}
		}
		else
		{
			return withArgs.getArgs();
		}
	}

	private String startInternal(ClusteredTask clusteredTask, String taskId)
	{
		ClusteredTaskWithArgs withArgs;
		if( clusteredTask.isTransient() )
		{
			String argsId = UUID.randomUUID().toString();
			Serializable[] args = clusteredTask.getArgs();
			taskArgs.put(argsId, args);
			withArgs = new ClusteredTaskWithArgs(clusteredTask, null, argsId, ourNodeId);
		}
		else
		{
			withArgs = new ClusteredTaskWithArgs(clusteredTask, clusteredTask.getArgs(), null, null);
		}
		byte[] taskBytes = PluginAwareObjectOutputStream.toBytes(withArgs);
		try
		{
			if( LOGGER.isTraceEnabled() )
			{
				LOGGER.trace("Serialized ClusteredTask size is " + taskBytes.length);
			}
			curator.create().creatingParentsIfNeeded().forPath(zookeeperService.getFullPath(ZK_TASKPATH, taskId),
				taskBytes);
		}
		catch( NodeExistsException nee )
		{
			LOGGER.debug("Task " + taskId + " already exists in ZK.");
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
		return taskId;
	}

	@Override
	public GlobalTaskStartInfo getGlobalTask(ClusteredTask globalTask, long millis)
	{
		if( !globalTask.isGlobal() )
			throw new Error("Isn't a global task");
		String globalId = globalTask.getGlobalId();
		String existingTaskId = getRunningGlobalTask(globalId);
		if( existingTaskId != null )
		{
			ensureGlobalTaskRunner(existingTaskId, globalTask);
			return new GlobalTaskStartInfo(existingTaskId, true);
		}
		String globalPath = zookeeperService.getFullPath(ZK_GLOBALTASKPATH, globalId);
		try
		{
			String taskId = UUID.randomUUID().toString();
			try
			{
				curator.create().creatingParentsIfNeeded().forPath(globalPath, taskId.getBytes());
				return new GlobalTaskStartInfo(startInternal(globalTask, taskId), false);
			}
			catch( NodeExistsException nee )
			{
				final String taskFromZk = new String(curator.getData().forPath(globalPath));
				//ensure there *is* a task for this
				ensureGlobalTaskRunner(taskFromZk, globalTask);
				return new GlobalTaskStartInfo(taskFromZk, false);
			}
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	/**
	 * 
	 * @return true if task was already running
	 */
	private boolean ensureGlobalTaskRunner(String taskId, ClusteredTask globalTask)
	{
		if( !isTaskActive(taskId) )
		{
			startInternal(globalTask, taskId);
			return false;
		}
		return true;
	}

	@Nullable
	@Override
	public TaskStatus getTaskStatus(String taskId)
	{
		TaskStatus status = getOrRequestStatus(taskId);
		if( status == null )
		{
			status = waitForTaskStatusInternal(taskId, 5000);
			if( status == null )
			{
				LOGGER.info("Requested task status for unknown taskId: " + taskId);
			}
		}
		return status;
	}

	private void requestFullStatus(Collection<String> taskIds)
	{
		if( !taskIds.isEmpty() )
		{
			LOGGER.debug("Requesting full status for tasks: " + taskIds);
			clusterMessagingService.postMessage(new AskFullStatusMessage(taskIds, ourNodeId));
		}
	}

	@Override
	public boolean haveTaskStatus(String taskId)
	{
		return statusHandler.getStatus(taskId) != null;
	}

	@Override
	public String getRunningGlobalTask(String globalId)
	{
		String globalPath = zookeeperService.getFullPath(ZK_GLOBALTASKPATH, globalId);
		ChildData data = globalCache.getCurrentData(globalPath);
		if( data != null )
		{
			return new String(data.getData());
		}
		return null;
	}

	private TaskStatus getOrRequestStatus(String taskId)
	{
		TaskStatus status = statusHandler.getStatus(taskId);
		if( status != null )
		{
			return status;
		}
		if( isTaskActive(taskId) )
		{
			requestFullStatus(Collections.singleton(taskId));
		}
		return null;
	}

	@Override
	public boolean isTaskActive(String taskId)
	{
		return taskCache.getCurrentData(zookeeperService.getFullPath(ZK_TASKPATH, taskId)) != null;
	}

	@Override
	public TaskStatus waitForTaskStatus(String taskId, long millis)
	{
		TaskStatus status = getOrRequestStatus(taskId);
		if( status == null )
		{
			return waitForTaskStatusInternal(taskId, millis);
		}
		return status;
	}

	private TaskStatus waitForTaskStatusInternal(String taskId, long millis)
	{
		long started = System.currentTimeMillis();
		long end = started + millis;
		while( true )
		{
			synchronized( statusHandler )
			{
				TaskStatus taskStatus = statusHandler.getStatus(taskId);
				if( taskStatus != null )
				{
					return taskStatus;
				}
				try
				{
					long now = System.currentTimeMillis();
					if( now < end )
					{
						statusHandler.wait(end - now);
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

	private boolean processRequest(String taskId, SimpleMessage message)
	{
		Task task = runnerHandler.getRunningTask(taskId);
		if( task != null )
		{
			task.postMessage(message);
			return true;
		}
		return false;
	}

	private boolean processResponse(SimpleMessage message)
	{
		BlockingQueue<SimpleMessage> waitingQueue = messageResponses.getIfPresent(message.getMessageId());
		if( waitingQueue != null )
		{
			messageResponses.invalidate(message.getMessageId());
			waitingQueue.add(message);
			return true;
		}
		return false;
	}

	public Collection<String> processAskUpdateMessage(final Collection<String> taskIds)
	{
		List<String> remainingTasks = Lists.newArrayList(taskIds);
		Iterator<String> iter = remainingTasks.iterator();
		while( iter.hasNext() )
		{
			String taskId = iter.next();
			Task task = runnerHandler.getRunningTask(taskId);
			if( task != null )
			{
				task.publishStatus();
				iter.remove();
			}
		}
		return remainingTasks;
	}

	@Override
	public void updateTaskStatus(Task task, Collection<TaskStatusChange<?>> statusChanges, String appliesTo,
		String becomes)
	{
		String taskId = task.getTaskId();
		statusHandler.updateStatus(taskId, statusChanges, appliesTo, becomes);
		LOGGER.debug("Sending status update message for taskId: " + taskId);
		clusterMessagingService.postMessage(new StatusChangesMessage(taskId, statusChanges, appliesTo, becomes));
	}

	@Override
	public void askTaskChanges(Collection<String> taskIds)
	{
		if( taskIds.isEmpty() )
		{
			return;
		}
		Collection<String> remainingAsks = processAskUpdateMessage(taskIds);
		if( !remainingAsks.isEmpty() )
		{
			clusterMessagingService.postMessage(new AskUpdatesMessage(remainingAsks));
		}
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T waitForTaskSubStatus(String taskId, String subStatus, long millis)
	{
		long started = System.currentTimeMillis();
		long end = started + millis;
		while( true )
		{
			synchronized( statusHandler )
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
						statusHandler.wait(end - now);
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
		postMessageInternal(taskId, message, false);
	}

	@Override
	public void messageResponse(Task task, SimpleMessage message)
	{
		if( !processResponse(message) )
		{
			clusterMessagingService.postMessage(new MsgMessage(null, message));
		}
	}

	@Override
	public <T> T postSynchronousMessage(String taskId, Serializable message, long millis)
	{
		BlockingQueue<SimpleMessage> blockingQueue = postMessageInternal(taskId, message, true);
		return waitForResponse(millis, blockingQueue);
	}

	private <T> T waitForResponse(long millis, BlockingQueue<SimpleMessage> blockingQueue)
	{
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

	private BlockingQueue<SimpleMessage> postMessageInternal(String taskId, Serializable message, boolean withResponse)
	{
		String messageId = UUID.randomUUID().toString();
		SimpleMessage msg = new SimpleMessage(messageId, message);

		BlockingQueue<SimpleMessage> blockingQueue;
		if( withResponse )
		{
			blockingQueue = createResponseQueue(messageId);
		}
		else
		{
			blockingQueue = null;
		}
		if( !processRequest(taskId, msg) )
		{
			clusterMessagingService.postMessage(new MsgMessage(taskId, msg));
		}
		return blockingQueue;
	}

	private BlockingQueue<SimpleMessage> createResponseQueue(String messageId)
	{
		BlockingQueue<SimpleMessage> blockingQueue = new LinkedBlockingQueue<SimpleMessage>(1);
		messageResponses.put(messageId, blockingQueue);
		return blockingQueue;
	}

	@Override
	public void addTaskStatusListener(String taskId, TaskStatusListener listener)
	{
		taskListeners.put(taskId, listener);
	}

	@Override
	public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception
	{
		Type type = event.getType();
		LOGGER.debug("Event = " + type);
		if( type == Type.CHILD_ADDED || type == Type.CHILD_REMOVED )
		{
			final boolean init;
			if( !startedInitialTasks )
			{
				synchronized( initialTasksLock )
				{
					init = startedInitialTasks;
				}
			}
			else
			{
				init = startedInitialTasks;
			}
			if( init )
			{
				String taskId = ZKPaths.getNodeFromPath(event.getData().getPath());
				switch( type )
				{
					case CHILD_ADDED:
						LOGGER.debug("Task added " + taskId);
						requestFullStatus(Collections.singleton(taskId));
						addRunner(taskId);
						break;
					case CHILD_REMOVED:
						LOGGER.debug("Task removed " + taskId);
						statusHandler.retireStatus(taskId);
						runnerHandler.deleteRunner(taskId, false);
						break;
				}
			}
			else
			{
				LOGGER.debug("Not init yet. " + event.getData().getPath());
			}
		}
		else
		{
			long sessionId = client.getZookeeperClient().getZooKeeper().getSessionId();
			if( type == Type.CONNECTION_SUSPENDED )
			{
				suspendedSessionId = sessionId;
			}
			else
				if( (type == Type.CONNECTION_RECONNECTED && suspendedSessionId != sessionId)
					|| type == Type.CONNECTION_LOST )
			{
				runnerHandler.lostConnection();
			}
			else if( type == Type.INITIALIZED )
			{
				synchronized( initialTasksLock )
				{
					startInitialTasks();
				}
			}
		}
	}

	private void startInitialTasks()
	{
		if( !startedInitialTasks )
		{
			List<ChildData> currentData = taskCache.getCurrentData();
			List<String> taskIds = Lists.newArrayList();
			for( ChildData task : currentData )
			{
				String taskId = ZKPaths.getNodeFromPath(task.getPath());
				LOGGER.debug("Task added " + taskId);
				taskIds.add(taskId);
				addRunner(taskId);
			}
			requestFullStatus(taskIds);
			startedInitialTasks = true;
		}
	}

	private void addRunner(String taskId)
	{
		ClusteredTaskWithArgs clusteredTaskWithArgs;
		try
		{
			String taskPath = zookeeperService.getFullPath(ZK_TASKPATH, taskId);
			byte[] taskBytes = curator.getData().forPath(taskPath);
			clusteredTaskWithArgs = (ClusteredTaskWithArgs) PluginAwareObjectInputStream.fromBytes(taskBytes);
			waitForDependencies(clusteredTaskWithArgs.getTask());
			runnerHandler.addRunner(taskId, clusteredTaskWithArgs);
		}
		catch( Exception e )
		{
			runnerHandler.addErroredRunner(taskId, e);
		}
	}

	@Override
	public Map<String, TaskStatus> getAllStatuses()
	{
		return statusHandler.getAllStatuses();
	}

	public class TaskStatusHandler
	{
		private final Logger LOGGER_STATUSES = LoggerFactory
			.getLogger(ClusteredTaskServiceImpl.class.getName() + ".TaskStatusHandler");
		private final Map<String, TaskStatusImpl> taskStatuses = Collections
			.synchronizedMap(new HashMap<String, TaskStatusImpl>());
		private final Cache<String, TaskStatusImpl> finishedStatuses = CacheBuilder.newBuilder().concurrencyLevel(4)
			.expireAfterAccess(30, TimeUnit.MINUTES).build();
		private final ExecutorService listenerThread = Executors
			.newSingleThreadExecutor(new NamedThreadFactory("TaskServiceImpl.listenerThread"));

		public synchronized Map<String, TaskStatus> getAllStatuses()
		{
			return Maps.<String, TaskStatus>newHashMap(taskStatuses);
		}

		public synchronized TaskStatusImpl getStatus(String taskId)
		{
			TaskStatusImpl status = finishedStatuses.getIfPresent(taskId);
			if( status != null )
			{
				return status;
			}
			return taskStatuses.get(taskId);
		}

		public synchronized void setStatus(String taskId, TaskStatusImpl status)
		{
			LOGGER_STATUSES.debug("Setting status for taskId: " + taskId + " finished=" + status.isFinished());
			TaskStatusImpl finishedStatus = finishedStatuses.getIfPresent(taskId);
			if( finishedStatus != null && !status.isFinished() )
			{
				LOGGER_STATUSES.debug("Attempting to use old status for taskId: " + taskId + ", ignoring");
				return;
			}
			if( status.isFinished() )
			{
				taskStatuses.remove(taskId);
				finishedStatuses.put(taskId, status);
			}
			else
			{
				finishedStatuses.invalidate(taskId);
				taskStatuses.put(taskId, status);
			}
			runListeners(taskId, status);
		}

		public synchronized void retireStatus(String taskId)
		{
			TaskStatusImpl status = taskStatuses.remove(taskId);
			if( status != null )
			{
				status.setFinished(true);
				finishedStatuses.put(taskId, status);
			}
		}

		public synchronized boolean updateStatus(final String taskId, Collection<TaskStatusChange<?>> changes,
			String appliesTo, String becomes)
		{
			final TaskStatusImpl taskStatus = getStatus(taskId);
			if( taskStatus == null )
			{
				LOGGER_STATUSES.warn("No status to update for taskId: " + taskId);
				return false;
			}
			if( !Objects.equals(taskStatus.getVersionString(), appliesTo) )
			{
				LOGGER_STATUSES.warn("Wrong version to update for taskId: " + taskId);
				return false;
			}
			synchronized( taskStatus )
			{
				for( TaskStatusChange<?> taskStatusChange : changes )
				{
					taskStatusChange.modifyStatus(taskStatus);
				}
				taskStatus.setVersionString(becomes);
			}
			runListeners(taskId, taskStatus);
			if( taskStatus.isFinished() )
			{
				retireStatus(taskId);
				taskListeners.removeAll(taskId);
			}
			return true;
		}

		private void runListeners(final String taskId, final TaskStatusImpl taskStatus)
		{
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
			notifyAll();
		}

		public boolean haveFinishedStatus(String taskIdFull)
		{
			return finishedStatuses.getIfPresent(taskIdFull) != null;
		}

		public TaskStatusImpl getExistingRunningStatus(String taskId)
		{
			return taskStatuses.get(taskId);
		}
	}

	public class TaskRunnerHandler
	{
		private final Logger LOGGER_RUNNER = LoggerFactory
			.getLogger(ClusteredTaskServiceImpl.class.getName() + ".TaskRunnerHandler");
		private final Map<String, TaskRunner> runningTasks = Maps.newHashMap();

		public synchronized void addErroredRunner(String taskId, Exception exception)
		{
			TaskRunner value = new TaskRunner(taskId, exception);
			runningTasks.put(taskId, value);
			taskRunnerExecutor.submit(value);
		}

		public synchronized void addRunner(String taskId, ClusteredTaskWithArgs taskWithArgs)
		{
			TaskRunner existingRunner = runningTasks.get(taskId);
			if( existingRunner != null )
			{
				throw new RuntimeException("Shouldn't be a TaskRunner for " + taskId + " here already");
			}

			TaskRunner value = new TaskRunner(taskId, taskWithArgs);
			runningTasks.put(taskId, value);

			ClusteredTask task = taskWithArgs.getTask();
			if( task.isPriority() )
			{
				priorityTaskRunnerExecutor.submit(value);
			}
			else
			{
				taskRunnerExecutor.submit(value);
			}
		}

		public synchronized void lostConnection()
		{
			List<TaskRunner> iter = Lists.newArrayList(runningTasks.values());
			for( TaskRunner runner : iter )
			{
				String taskId = runner.getTaskId();
				if( LOGGER_RUNNER.isDebugEnabled() )
				{
					LOGGER_RUNNER.debug("Refreshing Task runner for taskId:" + taskId);
				}
				runner.lostConnection();
			}
		}

		public void deleteRunner(String taskId, boolean lost)
		{
			TaskRunner runner = runningTasks.remove(taskId);
			if( runner != null )
			{
				runner.taskRemoved();
			}
		}

		public synchronized Task getRunningTask(String taskId)
		{
			TaskRunner runner = runningTasks.get(taskId);
			if( runner != null )
			{
				return runner.getIfRunning();
			}
			return null;
		}

		public class TaskRunner implements Callable<TaskResult>, CuratorWatcher
		{
			private final String taskId;
			private Task task;
			private final String taskOwnerPath;
			private ClusteredTask clusteredTask;
			private Thread threadForInterrupt;
			private boolean taskExists = true;
			private boolean owner;
			private boolean running;
			private String lockPath;
			private final Exception exception;

			//private final ClusteredTaskWithArgs taskWithArgs;

			public TaskRunner(String taskId, Exception exception)
			{
				this.taskId = taskId;
				this.taskOwnerPath = zookeeperService.getFullPath(ZK_TASKOWNERPATH, taskId);
				this.exception = exception;
				//this.taskWithArgs = null;
			}

			public TaskRunner(String taskId, ClusteredTaskWithArgs taskWithArgs)
			{
				this.taskId = taskId;
				this.taskOwnerPath = zookeeperService.getFullPath(ZK_TASKOWNERPATH, taskId);
				this.exception = null;
				//this.taskWithArgs = taskWithArgs;
			}

			public boolean isTaskExists()
			{
				return taskExists;
			}

			public boolean isOwner()
			{
				return owner;
			}

			public String getTaskId()
			{
				return taskId;
			}

			public String getGlobalId()
			{
				if( clusteredTask != null )
				{
					return clusteredTask.getGlobalId();
				}
				return null;
			}

			public void lostConnection()
			{
				lockPath = null;
				owner = false;
				interruptExisting();
			}

			private void interruptExisting()
			{
				if( task != null )
				{
					task.setShutdown(true);
				}
				if( threadForInterrupt != null )
				{
					LOGGER_RUNNER.debug("Interrupting task " + taskId);
					threadForInterrupt.interrupt();
				}
			}

			public void taskRemoved()
			{
				taskExists = false;
				interruptExisting();
			}

			public void close(Reaper reaper)
			{
				if( lockPath != null )
				{
					try
					{
						curator.delete().forPath(lockPath);
					}
					catch( Exception e )
					{
						reaper.addPath(lockPath);
					}
				}
			}

			private String nextLowest(long ours, List<String> all)
			{
				long lowest = ours;
				String lowestNode = null;
				for( String node : all )
				{
					long seq = getSequenceFromNode(node);
					if( seq != -1 && seq <= lowest )
					{
						lowest = seq;
						lowestNode = node;
					}
				}
				if( lowest == ours )
				{
					return null;
				}
				return lowestNode;
			}

			private long getSequenceFromNode(String node)
			{
				int ind = node.lastIndexOf('-');
				if( ind >= 0 )
				{
					return Long.parseLong(node.substring(ind + 1));
				}
				return -1;
			}

			@Override
			public void process(WatchedEvent event) throws Exception
			{
				synchronized( this )
				{
					notifyAll();
				}

			}

			@Override
			public TaskResult call() throws Exception
			{
				threadForInterrupt = Thread.currentThread();
				threadForInterrupt.setName("Waiting for ownership of task-" + taskId);
				owner = false;
				while( !owner && taskExists )
				{
					try
					{
						if( lockPath == null )
						{
							// for fairness
							Thread.sleep((long) (Math.random() * 100));
							lockPath = curator.create().creatingParentsIfNeeded().withProtection()
								.withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
								.forPath(ZKPaths.makePath(taskOwnerPath, "lock-"));
							if( LOGGER_RUNNER.isTraceEnabled() )
							{
								LOGGER_RUNNER.trace("Lock node for: " + taskId + " path: " + lockPath);
							}
						}
						String path = ZKPaths.getNodeFromPath(lockPath);
						List<String> childNodes = curator.getChildren().forPath(taskOwnerPath);
						long ourSeq = getSequenceFromNode(path);
						String nextLowest = nextLowest(ourSeq, childNodes);
						if( nextLowest != null )
						{
							String nextLowestFullPath = ZKPaths.makePath(taskOwnerPath, nextLowest);
							Stat stat = curator.checkExists().usingWatcher(this).forPath(nextLowestFullPath);
							if( stat != null )
							{
								if( stat.getEphemeralOwner() == curator.getZookeeperClient().getZooKeeper()
									.getSessionId() )
								{
									LOGGER_RUNNER
										.warn("Appears that owner of next lock is us, deleting: " + nextLowest);
									curator.delete().forPath(nextLowestFullPath);
								}
								else
								{
									if( LOGGER_RUNNER.isTraceEnabled() )
									{
										LOGGER_RUNNER
											.trace("Waiting on previous lock for: " + taskId + " : " + nextLowest);
									}
									synchronized( this )
									{
										wait(10000);
									}
								}
							}
						}
						else
						{
							owner = true;
						}
					}
					catch( InterruptedException ie )
					{
						// was interrupted
					}
					catch( KeeperException t )
					{
						LOGGER_RUNNER.debug("ZK problems during mutext acquisition: ", t.getMessage());
					}
					catch( Throwable t )
					{
						return new TaskResult(this, false, t, null);
					}
				}
				if( !taskExists )
				{
					return new TaskResult(this, false, null, null);
				}
				if( LOGGER_RUNNER.isDebugEnabled() )
				{
					LOGGER_RUNNER.debug("Mutex for taskId: " + taskId + " acquired");
				}

				if( exception != null )
				{
					return new TaskResult(this, true, exception, null);
				}

				// TODO: maybe remove this.  I don't think it's necessary now, 
				// given we pass the clusteredTaskWithArgs in the constructor, but better 
				// to be safe than sorry at this late stage.
				ClusteredTaskWithArgs clusteredTaskWithArgs;
				try
				{
					String taskPath = zookeeperService.getFullPath(ZK_TASKPATH, taskId);
					byte[] taskBytes = curator.getData().forPath(taskPath);
					clusteredTaskWithArgs = (ClusteredTaskWithArgs) PluginAwareObjectInputStream.fromBytes(taskBytes);
				}
				catch( ClassCastException cce )
				{
					return new TaskResult(this, true, cce, null);
				}
				catch( NoNodeException nne )
				{
					return new TaskResult(this, true, nne, null);
				}

				clusteredTask = clusteredTaskWithArgs.getTask();
				Throwable err = null;
				try
				{
					task = clusteredTask.createTask(getPluginService(), getArgsForTask(clusteredTaskWithArgs));
					task.setTaskDetails(ClusteredTaskServiceImpl.this, taskId);

					TaskStatusImpl initialStatus = statusHandler.getExistingRunningStatus(taskId);
					if( initialStatus != null )
					{
						LOGGER_RUNNER.info("Task was failed over: " + taskId);
						task.setUpdateVersion(initialStatus.getVersionString());
						task.setFailoverStatus(initialStatus);
						initialStatus.setNodeIdRunning(ourNodeId);
					}
					else
					{
						initialStatus = new TaskStatusImpl(ourNodeId, clusteredTask.getInternalId());
					}
					statusHandler.setStatus(taskId, initialStatus);
					clusterMessagingService
						.postMessage(new FullStatusResponseMessage(Collections.singletonMap(taskId, initialStatus)));
					LOGGER_RUNNER.info("Starting task with id:" + taskId + " class:" + task.getClass());
					running = true;
					task.call();
				}
				catch( Throwable t )
				{
					err = t;
					LOGGER_RUNNER.error("Task returned error:", t);
				}
				running = false;
				threadForInterrupt = null;
				TaskResult taskResult = new TaskResult(this, task == null || !task.isShutdown(), err, task);
				task = null;
				return taskResult;
			}

			public Task getIfRunning()
			{
				return running ? task : null;
			}
		}
	}

	public static class TaskResult
	{
		private final TaskRunner runner;
		private final Throwable error;
		private final boolean taskCompleted;
		private final Task completedTask;

		public TaskResult(TaskRunner runner, boolean taskCompleted, Throwable error, Task completedTask)
		{
			this.runner = runner;
			this.taskCompleted = taskCompleted;
			this.error = error;
			this.completedTask = completedTask;
		}

		public Task getCompletedTask()
		{
			return completedTask;
		}

		public Throwable getError()
		{
			return error;
		}

		public TaskRunner getRunner()
		{
			return runner;
		}

		public boolean isTaskCompleted()
		{
			return taskCompleted;
		}
	}

	public static abstract class TaskMessage implements Serializable
	{
		enum MsgType
		{
			ASK, UPDATES, FULL_STATUS, FULL_STATUS_RESPONSE, MSG, ARGS
		}

		private final MsgType type;

		public TaskMessage(MsgType type)
		{
			this.type = type;
		}

		public MsgType getType()
		{
			return type;
		}
	}

	public static class AskUpdatesMessage extends TaskMessage
	{
		private Collection<String> taskIds;

		public AskUpdatesMessage(Collection<String> taskIds)
		{
			super(MsgType.ASK);
			this.taskIds = taskIds;
		}

		public Collection<String> getTaskIds()
		{
			return taskIds;
		}
	}

	public static class AskFullStatusMessage extends TaskMessage
	{
		private Collection<String> taskIds;
		private String requestingId;

		public AskFullStatusMessage(Collection<String> taskIds, String requestingId)
		{
			super(MsgType.FULL_STATUS);
			this.taskIds = taskIds;
			this.requestingId = requestingId;
		}

		public Collection<String> getTaskIds()
		{
			return taskIds;
		}

		public String getRequestingId()
		{
			return requestingId;
		}
	}

	public static class FullStatusResponseMessage extends TaskMessage
	{
		private Map<String, TaskStatusImpl> statuses;

		public FullStatusResponseMessage(Map<String, TaskStatusImpl> statuses)
		{
			super(MsgType.FULL_STATUS_RESPONSE);
			this.statuses = statuses;
		}

		public Map<String, TaskStatusImpl> getStatuses()
		{
			return statuses;
		}
	}

	public static class StatusChangesMessage extends TaskMessage
	{
		private final String taskId;
		private final Collection<TaskStatusChange<?>> changes;
		private final String becomes;
		private final String appliesTo;

		public StatusChangesMessage(String taskId, Collection<TaskStatusChange<?>> changes, String appliesTo,
			String becomes)
		{
			super(MsgType.UPDATES);
			this.taskId = taskId;
			this.changes = changes;
			this.appliesTo = appliesTo;
			this.becomes = becomes;
		}

		public String getTaskId()
		{
			return taskId;
		}

		public Collection<TaskStatusChange<?>> getChanges()
		{
			return changes;
		}

		public String getBecomes()
		{
			return becomes;
		}

		public String getAppliesTo()
		{
			return appliesTo;
		}
	}

	public static class MsgMessage extends TaskMessage
	{
		private SimpleMessage msg;
		private String taskId;

		public MsgMessage(String taskId, SimpleMessage msg)
		{
			super(MsgType.MSG);
			this.taskId = taskId;
			this.msg = msg;
		}

		public SimpleMessage getMsg()
		{
			return msg;
		}

		public String getTaskId()
		{
			return taskId;
		}
	}

	public static class ArgsMessage extends TaskMessage
	{
		private String argsId;
		private String requestingId;
		private String responseId;
		private Serializable[] args;

		public ArgsMessage(String argsId, String requestingId)
		{
			super(MsgType.ARGS);
			this.argsId = argsId;
			this.requestingId = requestingId;
		}

		public ArgsMessage(String responseId, Serializable[] args)
		{
			super(MsgType.ARGS);
			this.responseId = responseId;
			this.args = args;
		}

		public String getRequestingId()
		{
			return requestingId;
		}

		public Serializable[] getArgs()
		{
			return args;
		}

		public String getArgsId()
		{
			return argsId;
		}

		public String getResponseId()
		{
			return responseId;
		}
	}

	protected void handleMessage(TaskMessage msg)
	{
		String taskId;
		switch( msg.getType() )
		{
			case ASK:
				AskUpdatesMessage askMsg = (AskUpdatesMessage) msg;
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Being asked for updates for tasks: " + askMsg.getTaskIds());
				}
				processAskUpdateMessage(askMsg.getTaskIds());
				break;
			case UPDATES:
				StatusChangesMessage smsg = (StatusChangesMessage) msg;
				taskId = smsg.getTaskId();
				LOGGER.debug("Receive status change for taskId: " + taskId);
				if( !statusHandler.updateStatus(taskId, smsg.getChanges(), smsg.getAppliesTo(), smsg.getBecomes()) )
				{
					requestFullStatus(Collections.singleton(taskId));
				}
				break;
			case MSG: {
				MsgMessage mmsg = (MsgMessage) msg;
				taskId = mmsg.getTaskId();
				SimpleMessage simpleMessage = mmsg.getMsg();
				if( taskId == null )
				{
					processResponse(simpleMessage);
				}
				else
				{
					processRequest(taskId, simpleMessage);
				}
			}
				break;
			case FULL_STATUS:
				AskFullStatusMessage fMsg = (AskFullStatusMessage) msg;
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Being asked by " + fMsg.getRequestingId() + " for full statuses of tasks: "
						+ fMsg.getTaskIds());
				}
				if( !fMsg.getRequestingId().equals(ourNodeId) )
				{
					Collection<String> taskIds = fMsg.getTaskIds();
					Map<String, TaskStatusImpl> fullStatuses = Maps.newHashMap();
					for( String taskIdFull : taskIds )
					{
						if( runnerHandler.getRunningTask(taskIdFull) != null
							|| statusHandler.haveFinishedStatus(taskIdFull) )
						{
							TaskStatusImpl status = statusHandler.getStatus(taskIdFull);
							if( status != null )
							{
								fullStatuses.put(taskIdFull, status);
							}
						}
					}
					if( !fullStatuses.isEmpty() )
					{
						clusterMessagingService.postMessage(fMsg.getRequestingId(),
							new FullStatusResponseMessage(fullStatuses));
					}
				}
				break;
			case FULL_STATUS_RESPONSE:
				FullStatusResponseMessage rMsg = (FullStatusResponseMessage) msg;
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Received full status responses for taskIds: " + rMsg.getStatuses().keySet());
				}
				for( Entry<String, TaskStatusImpl> status : rMsg.statuses.entrySet() )
				{
					statusHandler.setStatus(status.getKey(), status.getValue());
				}
				break;
			case ARGS:
				ArgsMessage argsMsg = (ArgsMessage) msg;
				String argsId = argsMsg.getArgsId();
				if( argsId != null )
				{
					Serializable[] args = taskArgs.getIfPresent(argsId);
					taskArgs.invalidate(argsId);
					clusterMessagingService.postMessage(argsMsg.getRequestingId(), new ArgsMessage(argsId, args));
				}
				else
				{
					String responseId = argsMsg.getResponseId();
					processResponse(new SimpleMessage(responseId, argsMsg));
				}
				break;
		}

	}

	public static class ClusteredTaskWithArgs implements Serializable
	{
		private final ClusteredTask task;
		private final Serializable[] args;
		private final String argsId;
		private final String nodeId;

		public ClusteredTaskWithArgs(ClusteredTask task, Serializable[] args, String argsId, String nodeId)
		{
			this.task = task;
			this.args = args;
			this.argsId = argsId;
			this.nodeId = nodeId;
		}

		public ClusteredTask getTask()
		{
			return task;
		}

		public Serializable[] getArgs()
		{
			return args;
		}

		public String getArgsId()
		{
			return argsId;
		}

		public String getNodeId()
		{
			return nodeId;
		}
	}

	@Override
	public Runnable canHandle(final Object msg)
	{
		if( msg instanceof TaskMessage )
		{
			return new Runnable()
			{
				@Override
				public void run()
				{
					handleMessage((TaskMessage) msg);
				}
			};
		}
		return null;
	}

	private class TaskWatchThread extends Thread
	{
		private final String name;
		private final CompletionService<TaskResult> executor;
		private final Reaper reaper;

		public TaskWatchThread(String name, CompletionService<TaskResult> executor, Reaper reaper)
		{
			this.name = name;
			this.executor = executor;
			this.reaper = reaper;
		}

		@Override
		public void run()
		{
			setName(name);
			while( true )
			{
				try
				{
					Future<TaskResult> result = executor.take();
					try
					{
						final TaskResult taskResult = result.get();
						final Throwable error = taskResult.getError();
						if( taskResult.isTaskCompleted() )
						{
							TaskRunner runner = taskResult.getRunner();
							Task task = taskResult.getCompletedTask();
							if( task != null )
							{
								try
								{
									Collection<TaskStatusChange<?>> changes = Lists
										.newArrayList(task.getCurrentChanges());
									changes.add(new FinishedStatusChange(error));
									updateTaskStatus(task, changes, task.getStatusVersion(),
										UUID.randomUUID().toString());
								}
								catch( Exception e )
								{
									LOGGER.error("Failed to send finished message", e);
								}
							}
							String taskId = runner.getTaskId();
							String globalId = runner.getGlobalId();
							String taskPath = zookeeperService.getFullPath(ZK_TASKPATH, taskId);
							String globalPath = globalId != null
								? zookeeperService.getFullPath(ZK_GLOBALTASKPATH, globalId) : null;

							if( error != null )
							{
								LOGGER.error("Error in task was:", error);
							}
							try
							{
								curator.delete().forPath(taskPath);
							}
							catch( NoNodeException nne )
							{
								//not there
							}
							catch( Exception ke )
							{
								LOGGER.warn(
									"Failed to delete path '" + taskPath + "' after completion, using reaper instead",
									ke);
								reaper.addPath(taskPath, Mode.REAP_UNTIL_GONE);
							}
							if( globalPath != null )
							{
								try
								{
									curator.delete().forPath(globalPath);
								}
								catch( NoNodeException nne )
								{
									//not there
								}
								catch( Exception ke )
								{
									LOGGER.warn("Failed to delete path '" + globalPath
										+ "' after completion, using reaper instead", ke);
									reaper.addPath(globalPath, Mode.REAP_UNTIL_GONE);
								}
							}
							reaper.addPath(zookeeperService.getFullPath(ZK_TASKOWNERPATH, taskId));
							runner.close(reaper);
						}
						else
						{
							TaskRunner runner = taskResult.getRunner();
							if( runner.isTaskExists() )
							{
								executor.submit(runner);
							}
							else
							{
								runner.close(reaper);
							}
							String errorMessage = error != null ? error.getMessage() : "(no msg)";
							LOGGER.trace("TaskRunner failed with exception: " + errorMessage);
						}
					}
					catch( ExecutionException e )
					{
						LOGGER.error("Error waiting for task completion", e);
					}
				}
				catch( InterruptedException e )
				{
					LOGGER.warn("Task finish thread interrupted, ignoring");
				}
				catch( Throwable t )
				{
					LOGGER.warn("Task finish thread got unknown error, restarting", t);
				}
			}
		}
	}
}
