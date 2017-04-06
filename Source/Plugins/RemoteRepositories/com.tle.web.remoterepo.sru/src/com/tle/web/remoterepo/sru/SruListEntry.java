/**
 * 
 */
package com.tle.web.remoterepo.sru;

import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.sru.service.impl.SruSearchResult;
import com.tle.web.remoterepo.RemoteRepoListEntry;

/**
 * @author larry
 */
@Bind
public class SruListEntry extends RemoteRepoListEntry<SruSearchResult>
{

	@Override
	protected String getKeyPrefix()
	{
		return null;
	}
}
