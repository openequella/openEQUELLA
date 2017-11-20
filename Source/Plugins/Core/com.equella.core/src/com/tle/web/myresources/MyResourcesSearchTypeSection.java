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

package com.tle.web.myresources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.search.filter.AbstractResetFiltersQuerySection;
import com.tle.web.search.filter.ResetFiltersParent;
import com.tle.web.search.filter.ResetFiltersSection;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@TreeIndexed
public class MyResourcesSearchTypeSection
	extends
		AbstractPrototypeSection<MyResourcesSearchTypeSection.MyResourcesSearchTypeModel>
	implements
		HtmlRenderer,
		ResetFiltersParent
{
	@PlugKey("myresources.menu")
	private static Label LABEL_TITLE;

	@Inject
	private MyResourcesListModel listModel;
	@Inject
	private ResetFiltersSection<?> resetFiltersSection;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private MyResourcesSearchResults searchResults;

	protected final List<SectionId> queryActionSections = new ArrayList<SectionId>();

	@Component(parameter = "type", supported = true, contexts = ContextableSearchSection.HISTORYURL_CONTEXT)
	private SingleSelectionList<MyResourcesSubSearch> searchType;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		listModel.register(tree, id);
		searchType.setListModel(listModel);
		searchType.setAlwaysSelect(true);
		searchType.addChangeEventHandler(events.getNamedHandler("subSearchChanged"));
		tree.registerInnerSection(resetFiltersSection, id);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		queryActionSections.addAll(tree.getChildIds(id));
	}

	public static void startSubSearch(SectionInfo from, String type, int subType)
	{
		SectionInfo info = RootMyResourcesSection.createForward(from);
		MyResourcesSearchTypeSection searchTypeSection = info.lookupSection(MyResourcesSearchTypeSection.class);
		MyResourcesSearchResults resultsSection = info.lookupSection(MyResourcesSearchResults.class);
		SingleSelectionList<MyResourcesSubSearch> searchType = searchTypeSection.getSearchType();
		searchType.setSelectedStringValue(info, type);
		if( subType != -1 )
		{
			MyResourcesSubSearch selectedSearch = searchType.getSelectedValue(info);
			selectedSearch.getSubSearches().get(subType).execute(info);
		}
		resultsSection.startSearch(info);
		from.forward(info);
	}

	@EventHandlerMethod
	public void subSearchChanged(SectionInfo info)
	{
		searchResults.startSearch(info);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		String selectedValue = searchType.getSelectedValueAsString(context);
		Set<String> matchingValues = searchType.getListModel().getMatchingValues(context,
			Collections.singleton(selectedValue));
		if( matchingValues.size() == 0 )
		{
			String defaultValue = searchType.getListModel().getDefaultValue(context);
			searchType.setSelectedStringValue(context, defaultValue);
		}

		getModel(context).setQueryActions(SectionUtils.renderSectionIds(context, queryActionSections));
		return viewFactory.createResult("myresourcessearchtype.ftl", this);
	}

	public SingleSelectionList<MyResourcesSubSearch> getSearchType()
	{
		return searchType;
	}

	public Label getHeaderTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public ResetFiltersSection<?> getResetFiltersSection()
	{
		return resetFiltersSection;
	}

	@Override
	public void addResetDiv(SectionTree tree, List<String> ajaxList)
	{
		resetFiltersSection.addAjaxDiv(ajaxList);
	}

	@Override
	public MyResourcesSearchTypeModel instantiateModel(SectionInfo info)
	{
		return new MyResourcesSearchTypeModel();
	}

	@Override
	public Class<MyResourcesSearchTypeModel> getModelClass()
	{
		return MyResourcesSearchTypeModel.class;
	}

	public class MyResourcesSearchTypeModel extends AbstractResetFiltersQuerySection.AbstractQuerySectionModel
	{
		// Nothing to see here
	}
}
