/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
