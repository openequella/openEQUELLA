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

package com.tle.web.remoterepo.section;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.beans.entity.FederatedSearch;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.SearchResults;
import com.tle.core.fedsearch.RemoteRepoSearchResult;
import com.tle.core.i18n.BundleCache;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.remoterepo.RemoteRepoListEntry;
import com.tle.web.remoterepo.RemoteRepoListEntryFactory;
import com.tle.web.remoterepo.RemoteRepoListItemViewHandlerCreator;
import com.tle.web.remoterepo.event.RemoteRepoSearchEvent;
import com.tle.web.remoterepo.event.RemoteRepoSearchResultEvent;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.template.Decorations;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@TreeIndexed
public abstract class RemoteRepoResultsSection<E extends RemoteRepoSearchEvent<E>, R extends RemoteRepoSearchResult, M extends RemoteRepoResultsSection.RemoteRepoResultsModel>
	extends
		AbstractSearchResultsSection<RemoteRepoListEntry<R>, E, RemoteRepoSearchResultEvent<R>, M>
{
	@TreeLookup
	private RemoteRepoListItemViewHandlerCreator<RemoteRepoListEntry<R>> viewResultCreator;
	@TreeLookup
	private AbstractRootRemoteRepoSection rootRemoteRepoSection;

	@Inject
	private RemoteRepoListSection<R> resultList;
	@Inject
	private RemoteRepoWebService repoWebService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private ConfigurationService configurationService;

	protected abstract SearchResults<R> doSearch(SectionInfo info, E search);

	protected abstract RemoteRepoListEntryFactory<R> getEntryFactory();

	@Override
	protected List<String> getStandardAjaxDivs()
	{
		return Lists.newArrayList("searchresults-cont", AbstractSearchResultsSection.DIV_SEARCHEADER);
	}

	protected void setupTitle(SectionInfo info)
	{
		Decorations.getDecorations(info).setTitle(
			new BundleLabel(repoWebService.getRemoteRepository(info).getName(), bundleCache));
	}

	@Override
	protected RemoteRepoSearchResultEvent<R> createResultsEvent(SectionInfo info, E searchEvent)
	{
		return makeSearchResultsEvent(doSearch(info, searchEvent));
	}

	@Override
	public RemoteRepoListSection<R> getItemList(SectionInfo info)
	{
		return resultList;
	}

	@Override
	public void processResults(SectionInfo info, RemoteRepoSearchResultEvent<R> results)
	{
		for( R result : results.getResults().getResults() )
		{
			resultList.addListItem(info, populateListItem(info, getEntryFactory().createListEntry(info, result)));
		}
	}

	@Override
	protected boolean showResults(RenderContext context, E searchEvent, RemoteRepoSearchResultEvent<R> resultsEvent)
	{
		if( resultsEvent == null && !configurationService.isAutoTestMode() )
		{
			return false;
		}
		return true;
	}

	@Override
	public E createSearchEvent(SectionInfo info)
	{
		final FederatedSearch search = repoWebService.getRemoteRepository(info);
		if( search.isDisabled() )
		{
			throw new AccessDeniedException(CurrentLocale.get("com.tle.web.fedsearch.disabled.error"));
		}
		if( getModel(info).isSearch() )
		{
			return makeSearchEvent(info, search);
		}
		return null;
	}

	protected RemoteRepoListEntry<R> populateListItem(SectionInfo info, RemoteRepoListEntry<R> listItem)
	{
		listItem.setView(getListItemViewHandler(info, listItem));
		return listItem;
	}

	protected Bookmark getListItemViewHandler(SectionInfo info, RemoteRepoListEntry<R> listItem)
	{
		return viewResultCreator.getViewHandler(info, listItem);
	}

	protected abstract E makeSearchEvent(SectionInfo info, FederatedSearch fedSearch);

	@SuppressWarnings({"unchecked", "rawtypes"})
	private RemoteRepoSearchResultEvent<R> makeSearchResultsEvent(SearchResults<R> results)
	{
		return new RemoteRepoSearchResultEvent(results);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(resultList, id);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "r";
	}

	public RemoteRepoListSection<R> getResultList()
	{
		return resultList;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new RemoteRepoResultsModel();
	}

	@Override
	public void startSearch(SectionInfo info)
	{
		super.startSearch(info);
		getModel(info).setSearch(true);
	}

	protected AbstractRootRemoteRepoSection getRootRemoteRepoSection()
	{
		return rootRemoteRepoSection;
	}

	public static class RemoteRepoResultsModel extends AbstractSearchResultsSection.SearchResultsModel
	{
		@Bookmarked
		private boolean search;

		public boolean isSearch()
		{
			return search;
		}

		public void setSearch(boolean search)
		{
			this.search = search;
		}
	}
}