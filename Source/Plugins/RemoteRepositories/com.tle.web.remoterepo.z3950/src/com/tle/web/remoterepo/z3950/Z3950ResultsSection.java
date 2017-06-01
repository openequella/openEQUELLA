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

package com.tle.web.remoterepo.z3950;

import javax.inject.Inject;

import com.tle.beans.entity.FederatedSearch;
import com.tle.common.searching.SearchResults;
import com.tle.core.remoterepo.z3950.Z3950SearchResult;
import com.tle.core.remoterepo.z3950.service.Z3950Service;
import com.tle.web.remoterepo.RemoteRepoListEntryFactory;
import com.tle.web.remoterepo.section.RemoteRepoResultsSection;
import com.tle.web.remoterepo.section.RemoteRepoResultsSection.RemoteRepoResultsModel;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public class Z3950ResultsSection
	extends
		RemoteRepoResultsSection<Z3950SearchEvent, Z3950SearchResult, RemoteRepoResultsModel>
{
	@Inject
	private Z3950Service z3950Service;
	@Inject
	private Z3950ListEntryFactory z3950Fac;

	@Override
	protected SearchResults<Z3950SearchResult> doSearch(SectionInfo info, Z3950SearchEvent search)
	{
		return z3950Service.search(search.getSearch(), search.getQuery(), search.getOffset(), search.getCount(),
			search.getAdvancedOptions());
	}

	@Override
	protected RemoteRepoListEntryFactory<Z3950SearchResult> getEntryFactory()
	{
		return z3950Fac;
	}

	@Override
	protected Z3950SearchEvent makeSearchEvent(SectionInfo info, FederatedSearch fedSearch)
	{
		return new Z3950SearchEvent(getRootRemoteRepoSection(), fedSearch);
	}
}
