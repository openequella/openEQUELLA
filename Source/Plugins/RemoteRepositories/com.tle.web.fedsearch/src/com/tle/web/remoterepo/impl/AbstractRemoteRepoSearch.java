package com.tle.web.remoterepo.impl;

import com.tle.beans.entity.FederatedSearch;
import com.tle.web.remoterepo.RemoteRepoSearch;
import com.tle.web.remoterepo.RemoteRepoSection;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public abstract class AbstractRemoteRepoSearch implements RemoteRepoSearch
{
	@Override
	public void forward(SectionInfo info, FederatedSearch search)
	{
		SectionInfo forward = info.createForward(getTreePath());

		RemoteRepoSection results = forward.lookupSection(RemoteRepoSection.class);
		results.setSearchUuid(forward, search.getUuid());

		info.forwardAsBookmark(forward);
	}

	protected abstract String getTreePath();
}
