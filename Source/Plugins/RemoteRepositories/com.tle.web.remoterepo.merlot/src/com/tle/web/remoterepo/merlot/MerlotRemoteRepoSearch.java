package com.tle.web.remoterepo.merlot;

import javax.inject.Singleton;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.MerlotSettings;
import com.tle.beans.search.SearchSettings;
import com.tle.core.guice.Bind;
import com.tle.web.remoterepo.RemoteRepoSearch;
import com.tle.web.remoterepo.RemoteRepoSection;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class MerlotRemoteRepoSearch implements RemoteRepoSearch
{
	@Override
	public void forward(SectionInfo info, FederatedSearch search)
	{
		SectionInfo forward = info.createForward("/access/merlot.do");

		RemoteRepoSection results = forward.lookupSection(RemoteRepoSection.class);
		results.setSearchUuid(forward, search.getUuid());

		info.forwardAsBookmark(forward);
	}

	@Override
	public SearchSettings createSettings(FederatedSearch search)
	{
		MerlotSettings settings = new MerlotSettings();
		settings.load(search);
		return settings;
	}

	@Override
	public String getContextKey()
	{
		return MerlotRootRemoteRepoSection.CONTEXT_KEY;
	}
}
