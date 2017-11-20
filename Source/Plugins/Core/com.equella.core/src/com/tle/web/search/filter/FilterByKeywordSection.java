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

package com.tle.web.search.filter;

import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.events.RenderEventContext;

@SuppressWarnings("nls")
@TreeIndexed
public class FilterByKeywordSection extends AbstractQuerySection<Object, FreetextSearchEvent>
	implements
		ResetFiltersListener
{
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

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
		searchButton.setClickHandler(searchResults.getRestartSearchHandler(tree));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("filter/filterbykeyword.ftl", context);
	}

	@Override
	protected boolean isIncludeUnfiltered()
	{
		return false;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "fbkw";
	}

	@Override
	public void reset(SectionInfo info)
	{
		queryField.setValue(info, null);
	}
}
