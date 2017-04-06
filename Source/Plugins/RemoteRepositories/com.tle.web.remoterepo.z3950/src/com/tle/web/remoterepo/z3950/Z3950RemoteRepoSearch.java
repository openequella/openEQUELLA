package com.tle.web.remoterepo.z3950;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SearchSettings;
import com.tle.beans.search.Z3950Settings;
import com.tle.core.guice.Bind;
import com.tle.web.remoterepo.impl.AbstractRemoteRepoSearch;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class Z3950RemoteRepoSearch extends AbstractRemoteRepoSearch
{
	@Override
	protected String getTreePath()
	{
		return "/access/z3950.do";
	}

	@Override
	public SearchSettings createSettings(FederatedSearch search)
	{
		Z3950Settings settings = new Z3950Settings();
		settings.load(search);
		return settings;
	}

	@Override
	public String getContextKey()
	{
		return Z3950RootRemoteRepoSection.CONTEXT_KEY;
	}
}
