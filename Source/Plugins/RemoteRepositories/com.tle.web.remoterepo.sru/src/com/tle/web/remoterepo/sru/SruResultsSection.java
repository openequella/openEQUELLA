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
