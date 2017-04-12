package com.tle.web.remoterepo.event;

import com.tle.core.fedsearch.RemoteRepoSearchResult;
import com.tle.web.sections.equella.search.event.SearchResultsListener;

/**
 * @author aholland
 */
public interface RemoteRepoSearchResultListener<R extends RemoteRepoSearchResult>
	extends
		SearchResultsListener<RemoteRepoSearchResultEvent<R>>
{
}