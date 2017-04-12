package com.tle.web.remoterepo.equella;

import javax.inject.Singleton;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SearchSettings;
import com.tle.beans.search.TLESettings;
import com.tle.core.guice.Bind;
import com.tle.web.remoterepo.impl.AbstractRemoteRepoSearch;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class EquellaRepoRemoteRepoSearch extends AbstractRemoteRepoSearch
{
	@Override
	protected String getTreePath()
	{
		return "/access/equella.do";
	}

	@Override
	public SearchSettings createSettings(FederatedSearch search)
	{
		TLESettings settings = new TLESettings();
		settings.load(search);
		return settings;
	}

	@Override
	public String getContextKey()
	{
		return EquellaRootRemoteRepoSection.CONTEXT_KEY;
	}
}
