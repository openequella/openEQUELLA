package com.tle.web.remoterepo;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SearchSettings;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public interface RemoteRepoSearch
{
	void forward(SectionInfo info, FederatedSearch search);

	SearchSettings createSettings(FederatedSearch search);

	String getContextKey();
}
