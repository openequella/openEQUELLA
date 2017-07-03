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

package com.tle.web.workflow.tasks.portal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.tle.core.guice.Bind;
import com.tle.web.workflow.portal.TaskListExtension;
import com.tle.web.workflow.portal.TaskListSubsearch;
import com.tle.web.workflow.tasks.FilterByAssignment.Assign;
import com.tle.web.workflow.tasks.portal.TaskListFilter.TaskListFilterFactory;

@Bind
@Singleton
public class TaskListPortalExtension implements TaskListExtension
{
	@Inject
	private TaskListFilterFactory filterFactory;

	@SuppressWarnings("nls")
	@Override
	public List<TaskListSubsearch> getTaskFilters()
	{
		Builder<TaskListSubsearch> taskFilters = ImmutableList.builder();
		taskFilters.add(filterFactory.create("taskall", Assign.ANY, false, false));
		taskFilters.add(filterFactory.create("taskme", Assign.ME, false, true));
		taskFilters.add(filterFactory.create("taskothers", Assign.OTHERS, false, true));
		taskFilters.add(filterFactory.create("tasknoone", Assign.NOONE, false, true));
		taskFilters.add(filterFactory.create("taskmust", Assign.ANY, true, true));
		return taskFilters.build();
	}
}
