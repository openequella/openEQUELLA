/**
 * 
 */
package com.tle.web.remoterepo.sru;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.sru.service.impl.SruSearchResult;
import com.tle.web.remoterepo.RemoteRepoListEntry;
import com.tle.web.remoterepo.RemoteRepoListEntryFactory;
import com.tle.web.sections.SectionInfo;

/**
 * @author larry
 */
@Bind
@Singleton
public class SruListEntryFactory implements RemoteRepoListEntryFactory<SruSearchResult>
{
	@Inject
	private Provider<SruListEntry> entryProvider;

	@Override
	public RemoteRepoListEntry<SruSearchResult> createListEntry(SectionInfo info, SruSearchResult result)
	{
		SruListEntry entry = entryProvider.get();
		entry.setResult(result);
		return entry;
	}
}
