package com.tle.web.cloud.search.section;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.cloud.search.CloudSearchListEntry;
import com.tle.web.cloud.search.CloudSearchListEntryFactory;
import com.tle.web.cloud.search.selection.CloudSelectItemListExtension;
import com.tle.web.itemlist.StandardListSection;
import com.tle.web.itemlist.item.AbstractItemlikeList;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension.ProcessEntryCallback;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
public class CloudSearchItemListSection
	extends
		AbstractItemlikeList<CloudItem, CloudSearchListEntry, CloudSearchItemListSection.CloudSearchItemListModel>
{
	@Inject
	private CloudSearchListEntryFactory entryFactory;
	@Inject
	private CloudSearchListAttachmentSection attachmentSection;
	@Inject
	private CloudSelectItemListExtension selectItemExtension;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		attachmentSection.register(tree, id);
		selectItemExtension.register(tree, id);
	}

	@Override
	protected void customiseListEntries(RenderContext context, List<CloudSearchListEntry> entries)
	{
		if( Check.isEmpty(entries) )
		{
			return;
		}
		final ListSettings<CloudSearchListEntry> settings = getModel(context).getListSettings();

		// Standard item list does this via extensions. Maybe refactor later to
		// do the same?
		attachmentSection.processEntries(context, entries, settings);
		final ProcessEntryCallback<CloudItem, CloudSearchListEntry> processEntries = selectItemExtension
			.processEntries(context, entries, settings);
		if( processEntries != null )
		{
			for( CloudSearchListEntry entry : entries )
			{
				processEntries.processEntry(entry);
			}
		}
	}

	@Override
	protected Set<String> getExtensionTypes()
	{
		return new HashSet<>();
	}

	@Override
	protected CloudSearchListEntry createItemListEntry(SectionInfo info, CloudItem item, FreetextResult result)
	{
		return entryFactory.createListEntry(info, item);
	}

	@Override
	public CloudSearchItemListModel instantiateModel(SectionInfo info)
	{
		return new CloudSearchItemListModel();
	}

	public static class CloudSearchItemListModel extends StandardListSection.Model<CloudSearchListEntry>
	{
		// Nothing
	}
}
