package com.tle.web.remoterepo;

import com.tle.core.fedsearch.RemoteRepoSearchResult;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public interface RemoteRepoListEntryFactory<R extends RemoteRepoSearchResult>
{
	RemoteRepoListEntry<R> createListEntry(SectionInfo info, R result);
}
