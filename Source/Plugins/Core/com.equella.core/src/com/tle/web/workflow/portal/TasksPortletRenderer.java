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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.tle.common.searching.Search;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * @author aholland / agibb
 */
@Bind
public class TasksPortletRenderer
	extends
		PortletContentRenderer<TasksPortletRenderer.WorkflowTasksPortletRendererModel>
{
	@ViewFactory
	private FreemarkerFactory view;
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private TaskListFilters filters;

	@EventFactory
	private EventGenerator events;

	@Override
	public SectionRenderable renderHtml(RenderEventContext context) throws Exception
	{
		Collection<TaskListSubsearch> taskFilters = filters.getFilters();
		List<Search> searches = new ArrayList<Search>();

		for( TaskListSubsearch filter : taskFilters )
		{
			searches.add(filter.getSearch());
		}

		int[] counts = freeTextService.countsFromFilters(searches);
		int i = 0;
		List<TaskRow> taskRows = getModel(context).getTasks();
		for( TaskListSubsearch filter : taskFilters )
		{
			TaskRow taskRow = new TaskRow(filter.getName(), counts[i++], new HtmlLinkState(events.getNamedHandler(
				"execSearch", filter.getIdentifier())), filter.isSecondLevel()); //$NON-NLS-1$
			taskRows.add(taskRow);
		}

		return view.createResult("portal/tasks.ftl", context); //$NON-NLS-1$
	}

	@EventHandlerMethod
	public void execSearch(SectionInfo info, String filter)
	{
		filters.execSearch(info, filter);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "ptl"; //$NON-NLS-1$
	}

	@Override
	public Class<WorkflowTasksPortletRendererModel> getModelClass()
	{
		return WorkflowTasksPortletRendererModel.class;
	}

	public static class WorkflowTasksPortletRendererModel
	{
		private final List<TaskRow> tasks = new ArrayList<TaskRow>();

		public List<TaskRow> getTasks()
		{
			return tasks;
		}
	}

	public static class TaskRow
	{
		private final Label label;
		private final int count;
		private final boolean secondLevel;
		private final HtmlComponentState link;

		public TaskRow(Label label, int count, HtmlComponentState link, boolean secondLevel)
		{
			this.label = label;
			this.count = count;
			this.link = link;
			this.secondLevel = secondLevel;
		}

		public Label getLabel()
		{
			return label;
		}

		public int getCount()
		{
			return count;
		}

		public HtmlComponentState getLink()
		{
			return link;
		}

		public boolean isSecondLevel()
		{
			return secondLevel;
		}
	}
}