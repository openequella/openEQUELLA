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

package com.tle.web.remoterepo.z3950;

import java.util.List;

import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.z3950.Z3950SearchResult;
import com.tle.web.itemlist.MetadataEntry;
import com.tle.web.remoterepo.RemoteRepoListEntry;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class Z3950ListEntry extends RemoteRepoListEntry<Z3950SearchResult>
{

	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(Z3950ListEntry.class);

	@Override
	protected String getKeyPrefix()
	{
		return resources.pluginId() + ".";
	}

	@Override
	public List<MetadataEntry> getMetadata()
	{
		final List<MetadataEntry> entries = super.getMetadata();
		addField(entries, "listentry.edition", result.getEdition());
		addField(entries, "listentry.isbn", result.getIsbn());
		addField(entries, "listentry.issn", result.getIssn());
		addField(entries, "listentry.publisher", result.getPublisher());
		addField(entries, "listentry.publishdate", result.getPublishDate());

		return entries;
	}
}
