package com.tle.web.remoterepo.srw;

import javax.inject.Singleton;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SRWSettings;
import com.tle.beans.search.SearchSettings;
import com.tle.core.guice.Bind;
import com.tle.web.remoterepo.impl.AbstractRemoteRepoSearch;

/**
 * @author aholland
 */
@Bind
@Singleton
public class SrwRemoteRepoSearch extends AbstractRemoteRepoSearch
{
	@Override
	protected String getTreePath()
	{
		return "/access/srw.do"; //$NON-NLS-1$
	}

	@Override
	public SearchSettings createSettings(FederatedSearch search)
	{
		SRWSettings settings = new SRWSettings();
		settings.load(search);
		return settings;
	}

	@Override
	public String getContextKey()
	{
		return SrwRootRemoteRepoSection.CONTEXT_KEY;
	}
}
