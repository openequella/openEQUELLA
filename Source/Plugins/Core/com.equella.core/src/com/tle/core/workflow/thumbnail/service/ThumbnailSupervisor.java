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

package com.tle.core.workflow.thumbnail.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.event.SchemaListener;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.services.GlobalTaskStartInfo;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.impl.AlwaysRunningTask;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.Task;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;

/**
 * 
 * @author Aaron
 *
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class ThumbnailSupervisor implements SchemaListener
{
	private static final Logger LOGGER = Logger.getLogger(ThumbnailSupervisor.class);

	private static final long SUPERVISOR_CHECK_PERIOD = TimeUnit.SECONDS.toMillis(20);
	private static final long TASK_WAIT_WARN_TIME = TimeUnit.MINUTES.toMillis(2);

	@Inject
	private RunAsInstitution runAs;
	@Inject
	private TaskService taskService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ThumbnailRequestService thumbRequestService;

	@Override
	public void systemSchemaUp()
	{
		taskService.getGlobalTask(new BeanClusteredTask("Thumbnail-Supervisor", true, ThumbnailSupervisor.class,
			"createSupervisorTask"), TimeUnit.MINUTES.toMillis(1));
	}

	@Override
	public void schemasUnavailable(Collection<Long> schemas)
	{
		// only care about system
	}

	@Override
	public void schemasAvailable(Collection<Long> schemas)
	{
		// only care about system
	}

	/**
	 * Factory method to create the one and only Thumbnail-Supervisor task
	 * @return
	 */
	public Task createSupervisorTask()
	{
		return new AlwaysRunningTask<Void>()
		{
			@Override
			protected Void waitFor()
			{
				try
				{
					Thread.sleep(SUPERVISOR_CHECK_PERIOD);
				}
				catch( InterruptedException ex )
				{
					// Don't care.
				}
				return null;
			}

			@Override
			public void runTask(Void ignore)
			{
				// Run any institution specific tasks first
				Collection<Institution> institutions = institutionService.getAvailableMap().values();
				for( final Institution inst : institutions )
				{
					runAs.executeAsSystem(inst, new Runnable()
					{
						@Override
						public void run()
						{
							doRunSupervisor(inst);
						}
					});
				}
			}

			@Override
			protected String getTitleKey()
			{
				return "com.tle.core.workflow.thumbnail.supervisor.title";
			}
		};
	}

	/**
	 * This gets invoked every SUPERVISOR_CHECK_PERIOD seconds, assuming it isn't already running.
	 * 
	 * @param inst
	 */
	public void doRunSupervisor(Institution inst)
	{
		final List<ThumbnailRequest> toRun = thumbRequestService.list(inst);
		if( Check.isEmpty(toRun) )
		{
			return;
		}

		// Request UUID to task ID
		final Map<String, String> runningTasks = Maps.newHashMap();

		// Start as many of the thumb tasks as possible in parallel
		// - let the cluster nodes balance it out. Once any task
		// finishes, check through the remaining tasks to see if more
		// can also be started. Keep looping until all the tasks have
		// been started and all finished.
		while( !toRun.isEmpty() || !runningTasks.isEmpty() )
		{
			for( Iterator<ThumbnailRequest> iter = toRun.iterator(); iter.hasNext(); )
			{
				final ThumbnailRequest request = iter.next();
				iter.remove();
				if( runningTasks.containsKey(request.getUuid()) )
				{
					break;
				}

				final String requestUuid = request.getUuid();
				final long uniqueId = inst.getUniqueId();
				final String globalId = "ThumbnailingTask-" + requestUuid + '-' + uniqueId;

				if( request.getTaskId() == null )
				{
					if( LOGGER.isTraceEnabled() )
					{
						LOGGER.trace("Task " + globalId + " just starting");
					}
				}
				else
				{
					final TaskStatus taskStatus = taskService.getTaskStatus(request.getTaskId());
					if( taskStatus == null )
					{
						LOGGER.info("Existing task for " + globalId + " but nothing running. Server restarted?");
					}
					else
					{
						if( LOGGER.isTraceEnabled() )
						{
							LOGGER.trace("Existing task for " + globalId + ", status: "
								+ (taskStatus.isFinished() ? "finished" : "not finished"));
						}
						break;
					}
				}

				final BeanClusteredTask bct = new BeanClusteredTask(globalId, ThumbnailService.class,
					"createThumbnailerTask", requestUuid, uniqueId, new ItemId(request.getItemUuid(),
						request.getItemVersion()), request.getHandle());
				final GlobalTaskStartInfo taskInfo = taskService.getGlobalTask(bct, TimeUnit.SECONDS.toMillis(30));
				final String taskId = taskInfo.getTaskId();
				runningTasks.put(request.getUuid(), taskId);

				if( taskInfo.isAlreadyRunning() )
				{
					LOGGER.trace("Task " + globalId + " already running.");
					break;
				}
				else
				{
					LOGGER.info("Submitted " + globalId + " with task ID " + taskId);
					request.setTaskId(taskId);
					request.setGlobalTaskId(globalId);
					thumbRequestService.update(request);
				}
			}

			// Wait for one of the currently running tasks to finish
			final Pair<String, TaskStatus> finishedStatus = waitForAnyTaskToFinish(runningTasks);
			if( finishedStatus != null )
			{
				final String finishedRequestUuid = finishedStatus.getFirst();
				final TaskStatus taskStat = finishedStatus.getSecond();
				runningTasks.remove(finishedRequestUuid);

				if( taskStat == null )
				{
					LOGGER.warn("Thumb request " + finishedRequestUuid + " didn't have a finished status.");
				}
				else
				{
					final String taskError = taskStat.getErrorMessage();
					if( taskError != null )
					{
						LOGGER.warn("Thumb task " + taskStat.getInternalId() + " errored. Removing the request "
							+ finishedRequestUuid);
						// In the future we could add code to increment a counter and re-try it at a future time.  Or not bother.
						thumbRequestService.delete(finishedRequestUuid);
					}
					else
					{
						LOGGER.debug("Aware of finished thumb task " + taskStat.getInternalId());
					}
				}
			}
		}
	}

	/**
	 * Waits for any of the given tasks to finish and returns that
	 * extension ID.
	 * 
	 * @param runningTasks maps of request UUIDs to task IDs.
	 * @return UUID of task that has finished.
	 */
	@Nullable
	private Pair<String, TaskStatus> waitForAnyTaskToFinish(Map<String, String> runningTasks)
	{
		final long start = System.currentTimeMillis();
		long checkStart = start;
		while( true )
		{
			if( runningTasks.isEmpty() )
			{
				return null;
			}

			final long now = System.currentTimeMillis();
			final long delta = now - checkStart;
			if( delta > TASK_WAIT_WARN_TIME )
			{
				checkStart = now;
				LOGGER.warn("Waiting for a task to finish for " + (now - start) + "ms");

				final StringBuilder rt = new StringBuilder("Current runningTasks: ");
				for( Map.Entry<String, String> runningTask : runningTasks.entrySet() )
				{
					rt.append(runningTask.getKey()).append("=").append(runningTask.getValue()).append(" (");
					final String taskId = runningTask.getValue();
					final TaskStatus status = taskService.waitForTaskStatus(taskId, TimeUnit.SECONDS.toMillis(1));
					if( status == null )
					{
						rt.append("no status) ");
					}
					else
					{
						rt.append("finished:").append(status.isFinished()).append(")");
					}
				}
				LOGGER.warn(rt.toString());
			}

			for( Map.Entry<String, String> runningTask : runningTasks.entrySet() )
			{
				final String taskId = runningTask.getValue();
				final TaskStatus status = taskService.waitForTaskStatus(taskId, TimeUnit.SECONDS.toMillis(1));
				final boolean fin = status != null ? status.isFinished() : !taskService.isTaskActive(taskId);
				if( fin )
				{
					return new Pair<>(runningTask.getKey(), status);
				}
			}
		}
	}
}
