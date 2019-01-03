/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.equella.component;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.AutocompleteDropdownRenderer;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.model.AnythingHtmlListModel;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.tle.web.sections.equella.render.EquellaDropdownExtension.AUTOCOMPLETE_RENDERER;

@Bind
@NonNullByDefault
public class CourseSelectionList extends SingleSelectionList<CourseInfo>
{
	private static final PluginResourceHelper resHelper = ResourcesService.getResourceHelper(CourseSelectionList.class);
	private static final IncludeFile INCLUDE = new IncludeFile(resHelper.url("scripts/component/courseselectionlist.js"));
	private static final JSCallAndReference EXTENSION = new ExternallyDefinedFunction("select2courseExtension", INCLUDE);

	@PlugKey("component.courseselectionlist.selectedcourse")
	private static String SELECTED_COURSE;

	@Inject
	private InstitutionService institutionService;
	@Inject
	private CourseInfoService courseInfoService;

	private boolean showArchived;

	public CourseSelectionList()
	{
		super();
		setListModel(new AnythingHtmlListModel<CourseInfo>()
		{
			@Override
			protected Option<CourseInfo> convertToOption(CourseInfo course)
			{
				return CourseSelectionList.this.convertToOption(course);
			}

			@Override
			public CourseInfo getValue(SectionInfo info, String value)
			{
				if (value != null)
				{
					return courseInfoService.getByUuid(value);
				}
				return null;
			}
		});
	}

	protected Option<CourseInfo> convertToOption(CourseInfo course)
	{
		final NameValue nv = new NameValue(CurrentLocale.get(SELECTED_COURSE, course.getCode(), CurrentLocale.get(course.getName())), course.getUuid());
		return new NameValueOption(nv, course);
	}

	@Override
	protected void extraHtmlRender(SectionInfo info)
	{
		super.extraHtmlRender(info);

		final HtmlListState listState = getModel(info);
		listState.setRendererType(AUTOCOMPLETE_RENDERER);
		listState.setAttribute(AutocompleteDropdownRenderer.AutocompleteDropdownRenderOptions.class,
				new AutocompleteDropdownRenderer.AutocompleteDropdownRenderOptions(){
			@Override
			public JSCallAndReference getExtension(PreRenderContext info)
			{
				return EXTENSION;
			}

			@Override
			public Map<String, Object> getParameters(PreRenderContext info)
			{
				final Map<String, Object> params = new HashMap<>();
				params.put("ajaxurl", institutionService.institutionalise("api/course"));
				params.put("ajaxurlparam", "q");
				if (showArchived)
				{
					params.put("showArchived", true);
				}
				return params;
			}
		});

		final CourseInfo course = getSelectedValue(info);
		if (course != null)
		{
			listState.setOptions(Collections.singletonList(convertToOption(course)));
		}
	}

	public boolean isShowArchived()
	{
		return showArchived;
	}

	public void setShowArchived(boolean showArchived)
	{
		ensureBuildingTree();
		this.showArchived = showArchived;
	}
}
