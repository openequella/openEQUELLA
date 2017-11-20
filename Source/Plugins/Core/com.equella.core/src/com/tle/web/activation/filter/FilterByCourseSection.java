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

import java.util.Collections;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.i18n.BundleCache;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

@NonNullByDefault
@SuppressWarnings("nls")
public class FilterByCourseSection extends AbstractPrototypeSection<FilterByCourseSection.FilterByCourseModel>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<FreetextSearchEvent>
{
	private static final String COURSE_DIV = "course";

	@Inject
	private CourseInfoService courseInfoService;
	@Inject
	private BundleCache bundleCache;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Inject
	@Component(name = "sc")
	private SelectCourseDialog selCourse;
	@Component(name = "r")
	private Link remove;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		selCourse
			.setOkCallback(searchResults.getResultsUpdater(tree, events.getEventHandler("courseSelected"), COURSE_DIV));
		remove.setClickHandler(new OverrideHandler(
			searchResults.getResultsUpdater(tree, events.getEventHandler("courseRemoved"), COURSE_DIV)));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		FilterByCourseModel model = getModel(context);
		String courseId = model.getCourseUuid();
		if( courseId != null )
		{
			model.setCourseName(new BundleLabel(courseInfoService.getByUuid(courseId).getName(), bundleCache));
		}
		return viewFactory.createResult("filter/filterbycourse.ftl", context);
	}

	@EventHandlerMethod
	public void courseSelected(SectionInfo info, String courseUuid)
	{
		getModel(info).setCourseUuid(courseUuid);
	}

	@EventHandlerMethod
	public void courseRemoved(SectionInfo info)
	{
		getModel(info).setCourseUuid(null);
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		String courseId = getModel(info).getCourseUuid();
		if( courseId != null )
		{
			event.getRawSearch().setCourses(Collections.singleton(courseInfoService.getByUuid(courseId)));
			event.setUserFiltered(true);
		}
	}

	@Override
	public Class<FilterByCourseModel> getModelClass()
	{
		return FilterByCourseModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "fbc";
	}

	public SelectCourseDialog getSelectCourse()
	{
		return selCourse;
	}

	@NonNullByDefault(false)
	public static class FilterByCourseModel
	{
		@Bookmarked(parameter = "course", supported = true)
		private String courseUuid;
		private Label courseName;

		public void setCourseName(Label courseName)
		{
			this.courseName = courseName;
		}

		public Label getCourseName()
		{
			return courseName;
		}

		public String getCourseUuid()
		{
			return courseUuid;
		}

		public void setCourseUuid(String courseUuid)
		{
			this.courseUuid = courseUuid;
		}
	}

	public Link getRemove()
	{
		return remove;
	}

	@Override
	public void reset(SectionInfo info)
	{
		courseRemoved(info);
	}
}
