/**
 * 
 */
package com.tle.web.remoterepo.sru;

import javax.inject.Singleton;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SRUSettings;
import com.tle.beans.search.SearchSettings;
import com.tle.core.guice.Bind;
import com.tle.web.remoterepo.impl.AbstractRemoteRepoSearch;

/**
 * @author larry
 */
@Bind
@Singleton
public class SruRemoteRepoSearch extends AbstractRemoteRepoSearch
{
	@Override
	protected String getTreePath()
	{
		return "/access/sru.do"; //$NON-NLS-1$
	}

	@Override
	public SearchSettings createSettings(FederatedSearch search)
	{
		SRUSettings settings = new SRUSettings();
		settings.load(search);
		return settings;
	}

	@Override
	public String getContextKey()
	{
		return SruRootRemoteRepoSection.CONTEXT_KEY;
	}
}
