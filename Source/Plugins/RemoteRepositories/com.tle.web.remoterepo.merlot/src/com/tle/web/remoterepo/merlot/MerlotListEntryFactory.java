package com.tle.web.remoterepo.merlot;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.merlot.service.impl.MerlotSearchResult;
import com.tle.web.remoterepo.RemoteRepoListEntry;
import com.tle.web.remoterepo.RemoteRepoListEntryFactory;
import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
@Bind
@Singleton
public class MerlotListEntryFactory implements RemoteRepoListEntryFactory<MerlotSearchResult>
{
	@Inject
	private Provider<MerlotListEntry> entryProvider;

	@Override
	public RemoteRepoListEntry<MerlotSearchResult> createListEntry(SectionInfo info, MerlotSearchResult result)
	{
		MerlotListEntry entry = entryProvider.get();
		entry.setResult(result);
		entry.setInfo(info);
		return entry;
	}
}
