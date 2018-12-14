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

package com.tle.web.activation.filter;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.component.CourseSelectionList;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;

import javax.inject.Inject;
import java.util.Collections;

@NonNullByDefault
@SuppressWarnings("nls")
public class FilterByCourseSection extends AbstractPrototypeSection<FilterByCourseSection.FilterByCourseModel>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<FreetextSearchEvent>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Inject
	@Component(name = "c", parameter = "course", supported = true)
	private CourseSelectionList selectCourse;
	@Component(name = "r")
	private Link clear;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		selectCourse.setShowArchived(true);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		selectCourse.addChangeEventHandler(new StatementHandler(searchResults.getResultsUpdater(tree, null, "courseFilter")));
		clear.setClickHandler(new OverrideHandler(
			searchResults.getResultsUpdater(tree, events.getEventHandler("courseCleared"), "courseFilter")));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final FilterByCourseModel model = getModel(context);
		model.setClearable(selectCourse.getSelectedValueAsString(context) != null);
		return viewFactory.createResult("filter/filterbycourse.ftl", context);
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		final CourseInfo selected = selectCourse.getSelectedValue(info);
		if( selected != null )
		{
			event.getRawSearch().setCourses(Collections.singleton(selected));
			event.setUserFiltered(true);
		}
	}

	@Override
	public Class<FilterByCourseModel> getModelClass()
	{
		return FilterByCourseModel.class;
	}

	@EventHandlerMethod
	public void courseCleared(SectionInfo info)
	{
		reset(info);
	}

	public SingleSelectionList<CourseInfo> getSelectCourse()
	{
		return selectCourse;
	}

	public Link getClear()
	{
		return clear;
	}

	@Override
	public void reset(SectionInfo info)
	{
		selectCourse.setSelectedValue(info, null);
	}

	public static class FilterByCourseModel
	{
		private boolean clearable;

		public boolean isClearable()
		{
			return clearable;
		}

		public void setClearable(boolean clearable)
		{
			this.clearable = clearable;
		}
	}
}
