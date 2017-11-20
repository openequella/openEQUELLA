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
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
public class SimpleResetFiltersQuerySection<SE extends AbstractSearchEvent<SE>>
	extends
		AbstractResetFiltersQuerySection<AbstractResetFiltersQuerySection.AbstractQuerySectionModel, SE>
{
	private static final String DIV_QUERY = "searchform";

	@PlugKey("query.search")
	private static Label SEARCH_LABEL;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		searchButton.setLabel(SEARCH_LABEL);
		searchButton.setClickHandler(searchResults.getResultsUpdater(tree, null, DIV_QUERY));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("filter/simplequery.ftl", this);
	}

	@Override
	public Class<AbstractQuerySectionModel> getModelClass()
	{
		return AbstractQuerySectionModel.class;
	}

	@Override
	protected String getAjaxDiv()
	{
		return DIV_QUERY;
	}
}
