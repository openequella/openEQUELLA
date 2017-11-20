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

package com.tle.core.scheduler.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.filters.AndFilter;
import com.tle.common.filters.Filter;
import com.tle.common.filters.OrFilter;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.event.SchemaListener;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ParamFilter;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.scheduler.SchedulerService;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.impl.AlwaysRunningTask;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.services.impl.Task;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.system.SystemConfigService;
import com.tle.core.system.service.SchemaDataSourceService;
import com.tle.core.xml.service.XmlService;

/**
 * @author Nicholas Read
 */
@Singleton
@SuppressWarnings("nls")
@Bind(SchedulerService.class)
public class SchedulerServiceImpl implements SchedulerService, SchemaListener
{
	private static final Logger LOGGER = Logger.getLogger(SchedulerServiceImpl.class);

	private static final long TASK_WAIT_WARN_TIME = TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(15);

	@Inject
	private ConfigurationService configService;
	@Inject
	private SystemConfigService systemConfigService;
	@Inject
	private XmlService xmlService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private TaskService taskService;
	@Inject
	private SchemaDataSourceService databaseSchemaService;

	private PluginService pluginService;
	private PluginTracker<ScheduledTask> taskTracker;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		this.pluginService = pluginService;

		taskTracker = new PluginTracker<ScheduledTask>(pluginService, "com.tle.core.scheduler", "scheduledTask", "id");
		taskTracker.setBeanKey("bean");
	}

	@Override
	public Schedules getSchedules()
	{
		return configService.getProperties(new Schedules());
	}

	@Override
	public void setSchedules(Schedules schedules)
	{
		configService.setProperties(schedules);
	}

	@Override
	public Schedules getServerSchedules()
	{
		String config = systemConfigService.getScheduledTasksConfig();
		if( !Check.isEmpty(config) )
		{
			try
			{
				return xmlService.deserialiseFromXml(getClass().getClassLoader(), config);
			}
			catch( Exception ex )
			{
				// Do nothing
			}
		}
		return new Schedules();
	}

	@Override
	public void setServerSchedules(Schedules schedules)
	{
		systemConfigService.setScheduleTasksConfig(xmlService.serialiseToXml(schedules));
	}

	public Task createSupervisorTask()
	{
		return new AlwaysRunningTask<Void>()
		{
			class TargetDates
			{
				Date dailyTarget;
				Date weeklyTarget;
				Date hourlyTarget;
			}

			private final LoadingCache<Institution, TargetDates> targetDates = CacheBuilder.newBuilder()
				.build(new CacheLoader<Institution, TargetDates>()
			{
				@Override
				public TargetDates load(Institution i)
				{
					TargetDates td = new TargetDates();
					Date never = new Date(Long.MAX_VALUE);
					td.dailyTarget = never;
					td.weeklyTarget = never;
					td.hourlyTarget = never;
					return td;
				}
			});

			@Override
			protected Void waitFor()
			{
				try
				{
					// Check more often than required since we're not going
					// to see configuration changes unless we wake-up and
					// read them in again.
					Thread.sleep(TimeUnit.MINUTES.toMillis(15));
				}
				catch( InterruptedException ex )
				{
					// Don't care if we get interrupted, just do our checks
					// and see if it's time anyway.
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
							List<Extension> toRun = getExtensionsToRun(inst);
							runExtensions(inst, toRun);
							updateSchedule(inst, getSchedules());
						}
					});
				}

				// Remove target dates for institutions that are not
				// currently enabled - we don't want them to be enabled
				// and suddenly have their tasks executed in case their
				// configuration has changed, etc..
				if( targetDates.size() > institutions.size() + 1 )
				{
					Set<Institution> activeInsts = Sets.newHashSet(institutions);
					activeInsts.add(Institution.FAKE);

					Set<Institution> inactiveInsts = Sets.difference(targetDates.asMap().keySet(), activeInsts);
					targetDates.invalidateAll(inactiveInsts);
				}

				// Now run any system tasks
				List<Extension> toRun = getExtensionsToRun(Institution.FAKE);
				runExtensions(Institution.FAKE, toRun);
				updateSchedule(Institution.FAKE, getServerSchedules());
			}

			@SuppressWarnings("unchecked")
			private List<Extension> getExtensionsToRun(Institution inst)
			{
				TargetDates td = targetDates.getUnchecked(inst);
				Date now = new Date();

				// Check if it is time to run anything at all
				boolean notNowThankYou = now.before(td.dailyTarget) && now.before(td.weeklyTarget)
					&& now.before(td.hourlyTarget);
				if( notNowThankYou )
				{
					return Collections.emptyList();
				}

				String scope = inst == Institution.FAKE ? "server" : "institution";
				Filter<Extension> f = new ParamFilter("scope", scope);

				List<ParamFilter> freqs = Lists.newArrayList();
				if( now.after(td.dailyTarget) )
				{
					freqs.add(new ParamFilter("frequency", "daily"));
				}
				if( now.after(td.weeklyTarget) )
				{
					freqs.add(new ParamFilter("frequency", "weekly"));
				}
				if( now.after(td.hourlyTarget) )
				{
					freqs.add(new ParamFilter("frequency", "hourly"));
				}
				if( !freqs.isEmpty() )
				{
					Filter<Extension>[] array = freqs.toArray(new ParamFilter[freqs.size()]);
					f = new AndFilter<Extension>(f, new OrFilter<Extension>(array));
				}

				return taskTracker.getExtensions(f);
			}

			private void updateSchedule(Institution inst, Schedules schedule)
			{
				TargetDates td = targetDates.getUnchecked(inst);
				Date now = new Date();

				// Work out next date for daily tasks. Explicit local variables
				// facilitate in-line debugging sometimes
				Calendar c = Calendar.getInstance();
				int scheduledHour = schedule.getDailyTaskHour();
				int scheduledMinute = 0;
				c.set(Calendar.HOUR_OF_DAY, scheduledHour);
				c.set(Calendar.MINUTE, scheduledMinute);
				c.set(Calendar.SECOND, 0);
				if( c.getTime().before(now) )
				{
					c.add(Calendar.DAY_OF_MONTH, 1);
				}
				td.dailyTarget = c.getTime();

				// Work out next date for weekly tasks
				c.setTime(now);
				c.set(Calendar.DAY_OF_WEEK, schedule.getWeeklyTaskDay() + 1);
				c.set(Calendar.HOUR_OF_DAY, schedule.getWeeklyTaskHour());
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				if( c.getTime().before(now) )
				{
					c.add(Calendar.WEEK_OF_YEAR, 1);
				}
				td.weeklyTarget = c.getTime();

				// Next hour
				c.setTime(now);
				c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
				td.hourlyTarget = c.getTime();
			}

			private void runExtensions(Institution inst, List<Extension> toRun)
			{
				if( Check.isEmpty(toRun) )
				{
					return;
				}

				// Order tasks based on dependencies
				ListMultimap<String, String> dependMap = ArrayListMultimap.create();
				for( Extension extension : toRun )
				{
					String extId = extension.getParameter("id").valueAsString();
					for( Parameter depend : extension.getParameters("depends") )
					{
						dependMap.put(extId, depend.valueAsString());
					}
					for( Parameter depend : extension.getParameters("before") )
					{
						dependMap.put(depend.valueAsString(), extId);
					}
				}

				// Extension ID to task ID
				Map<String, String> runningTasks = Maps.newHashMap();
				// Extension IDs
				Set<String> finishedTasks = Sets.newHashSet();

				// Start as many of the scheduled tasks as possible in parallel
				// - let the cluster nodes balance it out. Once any task
				// finishes, check through the remaining tasks to see if more
				// can also be started. Keep looping until all the tasks have
				// been started and all finished.
				while( !toRun.isEmpty() || !runningTasks.isEmpty() )
				{
					boolean startedTask = false;
					for( Iterator<Extension> iter = toRun.iterator(); iter.hasNext(); )
					{
						Extension extension = iter.next();
						String extId = extension.getParameter("id").valueAsString();
						List<String> deps = dependMap.get(extId);
						if( Check.isEmpty(deps) || finishedTasks.containsAll(deps) )
						{
							// All dependencies are finished, so start the task

							String taskId = startGlobalTask(extension, extId, inst);
							runningTasks.put(extId, taskId);
							iter.remove();
							startedTask = true;
						}
					}

					// Sanity check to prevent infinite loops. If we didn't
					// start any tasks, and there are no running tasks, then
					// there must be dependencies that are not defined. At the
					// moment, let's break the loop and kill everything - in the
					// future it would be better to be able to specify "weak"
					// dependencies where it doesn't matter if they're missing.
					if( !startedTask && runningTasks.isEmpty() )
					{
						throw logDependencyError(finishedTasks, dependMap);
					}

					// Wait for one of the currently running tasks to finish
					String finishedId = waitForAnyTaskToFinish(runningTasks);
					runningTasks.remove(finishedId);
					finishedTasks.add(finishedId);

					LOGGER.info("Aware of finished " + taskLog(finishedId, inst));
				}
			}

			/**
			 * Waits for any of the given tasks to finish and returns that
			 * extension ID.
			 * 
			 * @param runningTasks maps of extension IDs to task IDs.
			 * @return extension ID of task that has finished.
			 */
			private String waitForAnyTaskToFinish(Map<String, String> runningTasks)
			{
				final long start = System.currentTimeMillis();
				long checkStart = start;
				while( true )
				{
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
							String taskId = runningTask.getValue();
							TaskStatus status = taskService.waitForTaskStatus(taskId, TimeUnit.SECONDS.toMillis(1));
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
						String taskId = runningTask.getValue();
						TaskStatus status = taskService.waitForTaskStatus(taskId, TimeUnit.SECONDS.toMillis(1));
						if( status != null ? status.isFinished() : !taskService.isTaskActive(taskId) )
						{
							return runningTask.getKey();
						}
					}
				}
			}

			private RuntimeException logDependencyError(Set<String> finishedTasks,
				ListMultimap<String, String> unstartedTasks)
			{
				// Remove any entries where the dependency has finished or is
				// still to be started. The remaining keys should be tasks that
				// have non-existent dependencies.
				for( Iterator<Map.Entry<String, String>> iter = unstartedTasks.entries().iterator(); iter.hasNext(); )
				{
					Entry<String, String> entry = iter.next();
					String dep = entry.getValue();
					if( finishedTasks.contains(dep) || unstartedTasks.containsKey(dep) )
					{
						iter.remove();
					}
				}

				// Log each of the remaining tasks with the missing dependencies
				String msg = "There are " + unstartedTasks.size()
					+ " tasks that did not start as they're waiting on dependencies that"
					+ " don't seem to be defined.";
				LOGGER.error(msg + "  Logging them now:");
				for( String extId : unstartedTasks.keySet() )
				{
					LOGGER.error("Task " + extId + " has following dependencies that don't seem to exist "
						+ Joiner.on(",").join(unstartedTasks.get(extId)));
				}

				return new RuntimeException(msg + "  See the logs for more details.");
			}

			@Override
			protected String getTitleKey()
			{
				return "com.tle.core.scheduler.supervisor.title";
			}
		};
	}

	public Task createScheduledTask(final String pluginId, final String beanName, final long institutionId)
	{
		return new SingleShotTask()
		{
			@Override
			public void runTask()
			{
				final Institution inst = institutionId == Institution.FAKE.getUniqueId() ? Institution.FAKE
					: institutionService.getInstitution(institutionId);

				LOGGER.info("Starting execution of " + taskLog(beanName, inst));
				long t1 = System.currentTimeMillis();

				final ScheduledTask task = (ScheduledTask) pluginService.getBean(pluginId, beanName);
				if( inst != Institution.FAKE )
				{
					runAs.executeAsSystem(inst, new Callable<Void>()
					{
						@Override
						public Void call()
						{
							task.execute();
							return null;
						}
					});
				}
				else
				{
					for( Long schemaId : institutionService.getAvailableMap().keySet() )
					{
						databaseSchemaService.executeWithSchema(schemaId, new Callable<Void>()
						{
							@Override
							public Void call()
							{
								task.execute();
								return null;
							}
						});
					}
				}

				long t2 = System.currentTimeMillis();
				LOGGER.info("Finished execution of " + taskLog(beanName, inst) + " - it took a total of "
					+ TimeUnit.MILLISECONDS.toSeconds(t2 - t1) + " seconds");
			}

			@Override
			protected String getTitleKey()
			{
				return null;
			}
		};
	}

	private String taskLog(String id, Institution inst)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("scheduled task ");
		sb.append(id);
		if( inst != null && inst != Institution.FAKE )
		{
			sb.append(" for institution ");
			sb.append(inst.getName());
		}
		return sb.toString();
	}

	@Override
	public void systemSchemaUp()
	{
		ensureGlobalTaskStarted();
	}

	private void ensureGlobalTaskStarted()
	{
		taskService.getGlobalTask(
			new BeanClusteredTask("Scheduler-Supervisor", true, SchedulerService.class, "createSupervisorTask"),
			TimeUnit.MINUTES.toMillis(1));

	}

	private String startGlobalTask(Extension extension, String extId, Institution inst)
	{
		String pluginId = extension.getDeclaringPluginDescriptor().getId();
		String beanName = extension.getParameter("bean").valueAsString();
		long uniqueId = inst.getUniqueId();
		BeanClusteredTask bct = new BeanClusteredTask("Scheduled-Task-" + extId + '-' + uniqueId,
			SchedulerService.class, "createScheduledTask", pluginId, beanName, uniqueId);

		String taskId = taskService.getGlobalTask(bct, TimeUnit.SECONDS.toMillis(30)).getTaskId();

		LOGGER.info("Submitted " + taskLog(extId, inst) + " with task ID " + taskId);
		return taskId;
	}

	@Override
	@SecureOnCallSystem
	public void executeTaskNow(String extId)
	{
		Institution inst = CurrentInstitution.get();
		Extension extension = taskTracker.getExtension(extId);
		if( extension.getParameter("scope").valueAsString().equals("server") )
		{
			inst = Institution.FAKE;
		}
		startGlobalTask(extension, extId, inst);
	}

	@Override
	@SecureOnCallSystem
	public List<String> getAllSchedulerIds()
	{
		return Lists.newArrayList(taskTracker.getBeanMap().keySet());
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
}
