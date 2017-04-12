package com.tle.web.remoterepo.srw;

import com.tle.beans.entity.FederatedSearch;
import com.tle.web.remoterepo.event.RemoteRepoSearchEvent;
import com.tle.web.sections.SectionId;

public class SrwSearchEvent extends RemoteRepoSearchEvent<SrwSearchEvent>
{
	protected SrwSearchEvent(SectionId sectionId, FederatedSearch search)
	{
		super(sectionId, search);
	}
}
