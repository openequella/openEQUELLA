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

package com.tle.web.selection.home.model;

import java.util.List;

import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;

public class RootSelectionHomeModel
{
	private TemplateResult sections;
	private List<RecentSelectionSegmentModel> recentSegments;
	private List<SectionRenderable> quickSections;
	private boolean quickSearch;
	private String errorKey;

	public TemplateResult getSections()
	{
		return sections;
	}

	public void setSections(TemplateResult sections)
	{
		this.sections = sections;
	}

	public List<RecentSelectionSegmentModel> getRecentSegments()
	{
		return recentSegments;
	}

	public void setRecentSegments(List<RecentSelectionSegmentModel> recentSegments)
	{
		this.recentSegments = recentSegments;
	}

	public boolean isQuickSearch()
	{
		return quickSearch;
	}

	public void setQuickSearch(boolean quickSearch)
	{
		this.quickSearch = quickSearch;
	}

	public String getErrorKey()
	{
		return errorKey;
	}

	public void setErrorKey(String errorKey)
	{
		this.errorKey = errorKey;
	}

	public List<SectionRenderable> getQuickSections()
	{
		return quickSections;
	}

	public void setQuickSections(List<SectionRenderable> quickSections)
	{
		this.quickSections = quickSections;
	}
}