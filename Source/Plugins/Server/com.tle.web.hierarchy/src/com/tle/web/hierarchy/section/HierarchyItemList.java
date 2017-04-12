package com.tle.web.hierarchy.section;

import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.itemlist.item.StandardItemListEntryFactory;
import com.tle.web.sections.SectionInfo;

@Bind
public class HierarchyItemList
	extends
		AbstractItemList<StandardItemListEntry, AbstractItemList.Model<StandardItemListEntry>>
{
	@Inject
	private StandardItemListEntryFactory factory;

	@SuppressWarnings("nls")
	@Override
	protected Set<String> getExtensionTypes()
	{
		return ImmutableSet.of(ItemlikeListEntryExtension.TYPE_STANDARD, "hierarchy");
	}

	@Override
	protected StandardItemListEntry createItemListEntry(SectionInfo info, Item item, FreetextResult result)
	{
		return factory.createItemListItem(info, item, result);
	}
}
