package com.tle.web.remoterepo.event;

import com.tle.beans.entity.FederatedSearch;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

/**
 * @author aholland
 */
public abstract class RemoteRepoSearchEvent<E extends RemoteRepoSearchEvent<E>> extends AbstractSearchEvent<E>
{
	private final FederatedSearch search;

	protected RemoteRepoSearchEvent(SectionId sectionId, FederatedSearch search)
	{
		super(sectionId);
		this.search = search;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<E> listener) throws Exception
	{
		listener.prepareSearch(info, (E) this);
	}

	public FederatedSearch getSearch()
	{
		return search;
	}
}
