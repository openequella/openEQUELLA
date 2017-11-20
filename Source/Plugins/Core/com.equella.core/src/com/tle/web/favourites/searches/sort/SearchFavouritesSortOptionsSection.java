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

package com.tle.web.favourites.searches.sort;

import java.util.List;

import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.search.sort.SortOption;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class SearchFavouritesSortOptionsSection extends AbstractSortOptionsSection<FreetextSearchEvent>
{
	private static final String DATE_VALUE = "favdate"; //$NON-NLS-1$
	private static final String NAME_VALUE = "name"; //$NON-NLS-1$

	@PlugKey("sortoptions.searches.name")
	private static Label LABEL_NAME;
	@PlugKey("sortoptions.searches.date")
	private static Label LABEL_DATE;

	@Override
	protected void addSortOptions(List<SortOption> sorts)
	{
		sorts.add(new SortOption(LABEL_NAME, NAME_VALUE, NAME_VALUE, true));
		sorts.add(new SortOption(LABEL_DATE, DATE_VALUE, "date_modified", false)); //$NON-NLS-1$
	}

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		return DATE_VALUE;
	}
}
