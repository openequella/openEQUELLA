package com.tle.web.remoterepo.srw;

import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.srw.service.impl.SrwSearchResult;
import com.tle.web.remoterepo.RemoteRepoListEntry;

/**
 * @author aholland
 */
@Bind
public class SrwListEntry extends RemoteRepoListEntry<SrwSearchResult>
{
	@Override
	protected String getKeyPrefix()
	{
		return null;
	}
}
