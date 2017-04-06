package com.tle.web.itemlist.item;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

@TreeIndexed
@Bind
public class StandardItemList
	extends
		AbstractItemList<StandardItemListEntry, AbstractItemList.Model<StandardItemListEntry>>
{
	@Inject
	private StandardItemListEntryFactory factory;

	@Override
	protected Set<String> getExtensionTypes()
	{
		return Collections.singleton(ItemlikeListEntryExtension.TYPE_STANDARD);
	}

	@Override
	protected StandardItemListEntry createItemListEntry(SectionInfo info, Item item, FreetextResult result)
	{
		return factory.createItemListItem(info, item, result);
	}
}
