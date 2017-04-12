package com.tle.web.remoterepo.merlot;

import com.tle.core.fedsearch.RemoteRepoSearchResult;
import com.tle.web.remoterepo.event.RemoteRepoSearchResultEvent;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.search.PagingSection;

public class MerlotPagingSection
	extends
		PagingSection<MerlotRemoteRepoSearchEvent, RemoteRepoSearchResultEvent<RemoteRepoSearchResult>>
{
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		this.setRenderScreenOptions(false);
	}
}
