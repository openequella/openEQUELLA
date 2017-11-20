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

package com.tle.web.connectors.manage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.ExpiringValue;
import com.tle.common.NameValue;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.services.user.UserSessionService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

@NonNullByDefault
public class FilterByCourseSection extends AbstractPrototypeSection<FilterByCourseSection.Model>
	implements
		SearchEventListener<ConnectorManagementSearchEvent>,
		HtmlRenderer,
		ResetFiltersListener
{
	@PlugKey("manage.filter.course.select")
	private static String KEY_SELECT;

	private static final String SESSION_KEY = FilterByCourseSection.class.getName();

	@Inject
	private UserSessionService userSessionService;

	@ViewFactory
	protected FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;
	@TreeLookup
	private ConnectorManagementQuerySection querySection;
	@TreeLookup
	private FilterByArchivedSection filterByArchivedSection;

	@Inject
	private ConnectorRepositoryService repositoryService;

	@Component(parameter = "course", supported = true)
	private SingleSelectionList<ConnectorCourse> courseList;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		courseList.setListModel(new CoursesListModel());
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		JSHandler changeHandler = searchResults.getRestartSearchHandler(tree);
		courseList.addChangeEventHandler(changeHandler);
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( getModel(context).isDisabled() )
		{
			return null;
		}

		// TODO: the supportsExport check is not appropriate. Perhaps there
		// should be
		// a supportsCourses as well?
		Connector connector = querySection.getConnector(context);
		if( connector == null || !repositoryService.supportsExport(connector.getLmsType()) )
		{
			return null;
		}

		return viewFactory.createResult("filter/filterbycourse-connector.ftl", context);
	}

	@Override
	public void prepareSearch(SectionInfo info, ConnectorManagementSearchEvent connectorEvent) throws Exception
	{
		if( getModel(info).isDisabled() )
		{
			return;
		}
		ConnectorContentSearch search = connectorEvent.getSearch();

		ConnectorCourse course = courseList.getSelectedValue(info);
		if( course != null )
		{
			search.setCourse(course.getId());
			connectorEvent.setUserFiltered(true);
		}
	}

	public void disable(SectionInfo info)
	{
		getModel(info).setDisabled(true);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model(false);
	}

	protected static class Model
	{
		private boolean disabled;

		public Model(boolean disabled)
		{
			this.disabled = disabled;
		}

		public boolean isDisabled()
		{
			return disabled;
		}

		public void setDisabled(boolean disabled)
		{
			this.disabled = disabled;
		}
	}

	@Override
	public void reset(SectionInfo info)
	{
		courseList.setSelectedStringValue(info, null);
	}

	protected List<ConnectorCourse> getCourses(SectionInfo info) throws LmsUserNotFoundException
	{
		final Connector connector = querySection.getConnector(info);
		if( connector != null && repositoryService.supportsExport(connector.getLmsType()) )
		{
			final boolean showArchived = filterByArchivedSection.isArchived(info);
			final String key = SESSION_KEY + ":" + showArchived + ":" + connector.getUuid();
			ExpiringValue<List<ConnectorCourse>> cachedCourses = userSessionService.getAttribute(key);
			if( cachedCourses == null || cachedCourses.isTimedOut() )
			{
				cachedCourses = ExpiringValue.expireAfter(
					repositoryService.getAllCourses(connector, CurrentUser.getUsername(), showArchived), 30,
					TimeUnit.MINUTES);
				userSessionService.setAttribute(key, cachedCourses);
			}
			final List<ConnectorCourse> courses = new ArrayList<ConnectorCourse>(cachedCourses.getValue());
			return courses;
		}
		return Collections.emptyList();
	}

	public SingleSelectionList<ConnectorCourse> getCourseList()
	{
		return courseList;
	}

	public class CoursesListModel extends DynamicHtmlListModel<ConnectorCourse>
	{

		@Override
		protected Iterable<ConnectorCourse> populateModel(SectionInfo info)
		{
			try
			{
				return getCourses(info);
			}
			catch( LmsUserNotFoundException e )
			{
				throw Throwables.propagate(e);
			}
		}

		@Override
		protected Option<ConnectorCourse> convertToOption(SectionInfo info, ConnectorCourse course)
		{
			return new NameValueOption<ConnectorCourse>(new NameValue(course.getName(), course.getId()), course);
		}

		@Override
		protected Option<ConnectorCourse> getTopOption()
		{
			return new KeyOption<ConnectorCourse>(KEY_SELECT, "", null); //$NON-NLS-1$
		}
	}
}
