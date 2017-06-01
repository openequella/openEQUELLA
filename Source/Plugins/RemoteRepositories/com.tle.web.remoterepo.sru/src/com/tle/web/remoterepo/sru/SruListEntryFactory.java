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
