package com.tle.web.search.selection;

import javax.inject.Inject;

import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;

@Bind
public class SelectItemListExtension extends AbstractSelectItemListExtension<Item, ItemListEntry>
{
	@Inject
	private ViewableItemFactory vitemFactory;

	@Override
	protected ViewableItem<Item> getViewableItem(SectionInfo info, Item item)
	{
		return vitemFactory.createNewViewableItem(item.getItemId());
	}

	@Nullable
	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}
