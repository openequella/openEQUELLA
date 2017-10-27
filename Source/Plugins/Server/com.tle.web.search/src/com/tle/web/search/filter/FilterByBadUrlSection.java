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

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;

@Bind
@SuppressWarnings("nls")
public class FilterByBadUrlSection extends AbstractPrototypeSection<FilterByBadUrlSection.FilterByBadUrlModel>
	implements
		HtmlRenderer,
		SearchEventListener<FreetextSearchEvent>,
		ResetFiltersListener
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	private String layout = SearchResultsActionsSection.AREA_FILTER;

	@PlugKey("filter.bybadurl.checkbox")
	@Component(name = "url", parameter = "badurl", supported = true)
	private Checkbox filter;

	@Override
	public void registered(String id, SectionTree tree)
	{
		if( !Check.isEmpty(layout) )
		{
			tree.setLayout(id, layout);
		}
		super.registered(id, tree);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("filter/filterbybadurl.ftl", context);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		JSHandler searchHandler = new OverrideHandler(searchResults.getResultsUpdater(tree, null));
		filter.addClickStatements(searchHandler);
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		if( filter.isChecked(info) )
		{
			event.filterByTerm(false, FreeTextQuery.FIELD_IS_BAD_URL, "true");
		}
	}

	@Override
	public void reset(SectionInfo info)
	{
		filter.setChecked(info, false);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new FilterByBadUrlModel();
	}

	public static class FilterByBadUrlModel
	{
		//
	}

	public Checkbox getFilter()
	{
		return filter;
	}
}
