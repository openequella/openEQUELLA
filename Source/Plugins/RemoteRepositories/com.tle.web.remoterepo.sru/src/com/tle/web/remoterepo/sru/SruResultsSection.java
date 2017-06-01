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

package com.tle.web.remoterepo.sru;

import javax.inject.Inject;

import com.tle.beans.entity.FederatedSearch;
import com.tle.core.remoterepo.sru.service.SruService;
import com.tle.core.remoterepo.sru.service.impl.SruSearchResult;
import com.tle.core.remoterepo.sru.service.impl.SruSearchResults;
import com.tle.web.remoterepo.RemoteRepoListEntryFactory;
import com.tle.web.remoterepo.section.RemoteRepoResultsSection;
import com.tle.web.remoterepo.section.RemoteRepoResultsSection.RemoteRepoResultsModel;
import com.tle.web.sections.SectionInfo;

/**
 * @author larry
 */
public class SruResultsSection
	extends
		RemoteRepoResultsSection<SruSearchEvent, SruSearchResult, RemoteRepoResultsModel>
{
	@Inject
	private SruService sruService;
	@Inject
	private SruListEntryFactory sruFac;

	@Override
	protected SruSearchResults doSearch(SectionInfo info, SruSearchEvent search)
	{
		return sruService.search(search.getSearch(), search.getQuery(), search.getOffset(), search.getCount());
	}

	@Override
	protected RemoteRepoListEntryFactory<SruSearchResult> getEntryFactory()
	{
		return sruFac;
	}

	@Override
	protected SruSearchEvent makeSearchEvent(SectionInfo info, FederatedSearch fedSearch)
	{
		return new SruSearchEvent(getRootRemoteRepoSection(), fedSearch);
	}
}
