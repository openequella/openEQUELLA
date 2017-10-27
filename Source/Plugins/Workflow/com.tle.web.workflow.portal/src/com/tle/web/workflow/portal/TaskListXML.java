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

package com.tle.web.workflow.portal;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.tle.common.workflow.TaskFilterCount;
import com.tle.core.guice.Bind;
import com.tle.core.xml.service.XmlService;

@Bind
@Singleton
public class TaskListXML
{
	@Inject
	private XmlService xmlService;
	private XStream xstream;

	public static class TaskFilters
	{
		private final List<TaskFilterCount> filters;

		public TaskFilters(List<TaskFilterCount> filterList)
		{
			this.filters = filterList;
		}

		public List<TaskFilterCount> getFilters()
		{
			return filters;
		}
	}

	public static class ItemTask
	{
		private final String itemUuid;
		private final int itemVersion;
		private final String taskUuid;

		public ItemTask(String itemUuid, int itemVersion, String taskUuid)
		{
			this.itemUuid = itemUuid;
			this.itemVersion = itemVersion;
			this.taskUuid = taskUuid;
		}

		public String getItemUuid()
		{
			return itemUuid;
		}

		public int getItemVersion()
		{
			return itemVersion;
		}

		public String getTaskUuid()
		{
			return taskUuid;
		}

	}

	public static class ItemTasks
	{
		private final List<ItemTask> tasks;

		public ItemTasks(List<ItemTask> tasks)
		{
			this.tasks = tasks;
		}

		public List<ItemTask> getFilters()
		{
			return tasks;
		}
	}

	@SuppressWarnings("nls")
	@PostConstruct
	public void setupXStream()
	{
		xstream = xmlService.createDefault(getClass().getClassLoader());
		xstream.alias("filters", TaskFilters.class);
		xstream.alias("filter", TaskFilterCount.class);
		xstream.alias("task", ItemTask.class);
		xstream.alias("tasks", ItemTasks.class);
		xstream.addImplicitCollection(TaskFilters.class, "filters");
		xstream.addImplicitCollection(ItemTasks.class, "tasks");
	}

	public String toXML(Object obj)
	{
		return xstream.toXML(obj);
	}

}
