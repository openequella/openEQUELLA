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

package com.tle.web.remoterepo.merlot;

import javax.inject.Inject;

import com.tle.beans.entity.FederatedSearch;
import com.tle.common.searching.SearchResults;
import com.tle.core.remoterepo.merlot.service.MerlotService;
import com.tle.core.remoterepo.merlot.service.impl.MerlotSearchResult;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.RemoteRepoListEntryFactory;
import com.tle.web.remoterepo.event.RemoteRepoSearchResultEvent;
import com.tle.web.remoterepo.section.RemoteRepoResultsSection;
import com.tle.web.remoterepo.section.RemoteRepoResultsSection.RemoteRepoResultsModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class MerlotResultsSection
	extends
		RemoteRepoResultsSection<MerlotRemoteRepoSearchEvent, MerlotSearchResult, RemoteRepoResultsModel>
{
	@Inject
	private MerlotService merlotService;
	@Inject
	private MerlotListEntryFactory entryFac;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	protected SectionRenderable getFooter(RenderContext context, MerlotRemoteRepoSearchEvent searchEvent,
		RemoteRepoSearchResultEvent<MerlotSearchResult> resultsEvent)
	{
		if( resultsEvent != null && resultsEvent.getCount() > 0 )
		{
			return view.createResult("merlotfooter.ftl", this);
		}
		return null;
	}

	@Override
	protected SearchResults<MerlotSearchResult> doSearch(SectionInfo info, MerlotRemoteRepoSearchEvent search)
	{
		return merlotService.search(search, search.getOffset(), search.getCount());
	}

	@Override
	protected MerlotRemoteRepoSearchEvent makeSearchEvent(SectionInfo info, FederatedSearch fedSearch)
	{
		return new MerlotRemoteRepoSearchEvent(getRootRemoteRepoSection(), fedSearch);
	}

	@Override
	protected RemoteRepoListEntryFactory<MerlotSearchResult> getEntryFactory()
	{
		return entryFac;
	}

	@Override
	public Class<RemoteRepoResultsModel> getModelClass()
	{
		return RemoteRepoResultsModel.class;
	}
}
