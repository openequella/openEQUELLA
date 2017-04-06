package com.tle.web.remoterepo.z3950;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.z3950.Z3950SearchResult;
import com.tle.web.remoterepo.RemoteRepoListEntry;
import com.tle.web.remoterepo.RemoteRepoListEntryFactory;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
@Bind
@Singleton
public class Z3950ListEntryFactory implements RemoteRepoListEntryFactory<Z3950SearchResult>
{
	@Inject
	private Provider<Z3950ListEntry> entryProvider;

	@Override
	public RemoteRepoListEntry<Z3950SearchResult> createListEntry(SectionInfo info, Z3950SearchResult result)
	{
		Z3950ListEntry entry = entryProvider.get();
		entry.setResult(result);
		return entry;
	}

}
