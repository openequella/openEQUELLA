package com.tle.web.remoterepo.srw;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.srw.service.impl.SrwSearchResult;
import com.tle.web.remoterepo.RemoteRepoListEntry;
import com.tle.web.remoterepo.RemoteRepoListEntryFactory;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
@Bind
@Singleton
public class SrwListEntryFactory implements RemoteRepoListEntryFactory<SrwSearchResult>
{
	@Inject
	private Provider<SrwListEntry> entryProvider;

	@Override
	public RemoteRepoListEntry<SrwSearchResult> createListEntry(SectionInfo info, SrwSearchResult result)
	{
		SrwListEntry entry = entryProvider.get();
		entry.setResult(result);
		return entry;
	}

}
