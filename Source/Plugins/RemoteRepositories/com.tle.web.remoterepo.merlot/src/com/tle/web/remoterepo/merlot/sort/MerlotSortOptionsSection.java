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

package com.tle.web.remoterepo.merlot.sort;

import java.util.List;

import javax.inject.Inject;

import com.tle.web.remoterepo.merlot.MerlotRemoteRepoSearchEvent;
import com.tle.web.remoterepo.merlot.MerlotWebService;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.search.sort.SortOption;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
public class MerlotSortOptionsSection extends AbstractSortOptionsSection<MerlotRemoteRepoSearchEvent>
{
	@PlugKey("sort.rating")
	private static Label RATING_LABEL;
	@PlugKey("sort.title")
	private static Label TITLE_LABEL;
	@PlugKey("sort.author")
	private static Label AUTHOR_LABEL;
	@PlugKey("sort.materialtype")
	private static Label MATERIAL_LABEL;
	@PlugKey("sort.datecreated")
	private static Label DATE_LABEL;

	@Inject
	private MerlotWebService merlotWebService;

	@Override
	protected void addSortOptions(List<SortOption> sorts)
	{
		sorts.add(new SortOption(RATING_LABEL, "overallRating", null));
		sorts.add(new SortOption(TITLE_LABEL, "title", null));
		sorts.add(new SortOption(AUTHOR_LABEL, "author", null));
		sorts.add(new SortOption(MATERIAL_LABEL, "materialtype", null));
		sorts.add(new SortOption(DATE_LABEL, "dateCreated", null));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !merlotWebService.getSettings(context).isAdvancedApi() )
		{
			return null;
		}
		if( sortOptions.getSelectedValue(context) == null )
		{
			sortOptions.setSelectedStringValue(context, "overallRating");
		}

		// No reverse for MERLOT
		reverse.setDisplayed(context, false);

		return super.renderHtml(context);
	}

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		return null;
	}

	@Override
	public void prepareSearch(SectionInfo info, MerlotRemoteRepoSearchEvent event)
	{
		// Different sort
		event.setSort(sortOptions.getSelectedValueAsString(info));
	}
}
