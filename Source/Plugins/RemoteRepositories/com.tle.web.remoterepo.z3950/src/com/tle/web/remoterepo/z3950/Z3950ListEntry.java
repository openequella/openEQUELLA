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
