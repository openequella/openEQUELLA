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

package com.tle.web.cloud.search.sort;

import java.util.List;

import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SortField;
import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.search.sort.SortOption;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
public class CloudSortOptionsSection extends AbstractSortOptionsSection<CloudSearchEvent>
{
	@PlugKey("sort.relevance")
	private static Label RELEVANCE_LABEL;
	@PlugKey("sort.lastmodified")
	private static Label MODIFIED_LABEL;

	@Override
	protected void addSortOptions(List<SortOption> sorts)
	{
		sorts.add(new SortOption(RELEVANCE_LABEL, "relevance", null)
		{
			@Override
			public SortField[] createSort()
			{
				return new SortField[]{new SortField("relevance", false)};
			}
		});
		sorts.add(new SortOption(MODIFIED_LABEL, "modified", null)
		{
			@Override
			public SortField[] createSort()
			{
				return new SortField[]{new SortField("modified", false)};
			}
		});
		sorts.add(new SortOption(SortType.NAME));
		sorts.add(new SortOption(SortType.RATING));
	}

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		return null;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		boolean relSel = sortOptions.getSelectedValueAsString(context).equals("relevance");
		if( relSel )
		{
			reverse.setChecked(context, false);
		}
		reverse.setDisabled(context, relSel);
		return super.renderHtml(context);
	}
}
