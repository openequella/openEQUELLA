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

package com.tle.web.search.sort;

import java.util.List;

import com.tle.common.Check;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SortField;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;

public abstract class StandardSortOptionsSection<SE extends AbstractSearchEvent<SE>>
	extends
		AbstractSortOptionsSection<SE>
{
	private static final SortOption OPTION_DATEMODIFIED = new SortOption(SortType.DATEMODIFIED);
	private static final SortOption OPTION_RANK = new SortOption(SortType.RANK);
	@TreeLookup
	private AbstractQuerySection<?, ?> querySection;

	@Override
	protected void addSortOptions(List<SortOption> sorts)
	{
		sorts.add(OPTION_RANK);
		sorts.add(OPTION_DATEMODIFIED);
		sorts.add(new SortOption(SortType.DATECREATED));
		sorts.add(new SortOption(SortType.NAME));
		sorts.add(new SortOption(SortType.RATING));
	}

	@Override
	protected SortField[] createSortFromOption(SectionInfo info, SortOption selOpt)
	{
		if( selOpt.equals(OPTION_RANK) && Check.isEmpty(querySection.getParsedQuery(info)) )
		{
			selOpt = OPTION_DATEMODIFIED;
		}
		return selOpt.createSort();
	}
}
