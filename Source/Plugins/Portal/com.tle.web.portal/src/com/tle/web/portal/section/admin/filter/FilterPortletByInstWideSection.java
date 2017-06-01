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

package com.tle.web.portal.section.admin.filter;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.section.admin.PortletSearchEvent;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

public class FilterPortletByInstWideSection extends AbstractPrototypeSection<Object>
	implements
		SearchEventListener<PortletSearchEvent>,
		HtmlRenderer,
		ResetFiltersListener
{
	@ViewFactory
	protected FreemarkerFactory viewFactory;

	@Component(name = "iw", parameter = "iw", supported = true)
	private SingleSelectionList<Boolean> instWide;

	@PlugKey("filter.byinstwide.all")
	private static String ALL_PORTLETS;
	@PlugKey("filter.byinstwide.inst")
	private static String INST_PORTLETS;
	@PlugKey("filter.byinstwide.user")
	private static String USER_PORTLETS;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;
	private JSHandler changeHandler;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		SimpleHtmlListModel<Boolean> model = new SimpleHtmlListModel<Boolean>();
		model.add(new KeyOption<Boolean>(ALL_PORTLETS, "all", null)); //$NON-NLS-1$
		model.add(new KeyOption<Boolean>(INST_PORTLETS, "inst", true)); //$NON-NLS-1$
		model.add(new KeyOption<Boolean>(USER_PORTLETS, "user", false)); //$NON-NLS-1$
		instWide.setListModel(model);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("admin/filter/filterbyinstwide.ftl", context);
	}

	@Override
	public void prepareSearch(SectionInfo info, PortletSearchEvent event) throws Exception
	{
		event.filterByOnlyInstWide(instWide.getSelectedValue(info));
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		if( changeHandler == null )
		{
			changeHandler = searchResults.getRestartSearchHandler(tree);
		}
		instWide.addEventStatements(JSHandler.EVENT_CHANGE, changeHandler);
	}

	public SingleSelectionList<Boolean> getInstWide()
	{
		return instWide;
	}

	@Override
	public void reset(SectionInfo info)
	{
		instWide.setSelectedStringValue(info, ALL_PORTLETS);
	}
}