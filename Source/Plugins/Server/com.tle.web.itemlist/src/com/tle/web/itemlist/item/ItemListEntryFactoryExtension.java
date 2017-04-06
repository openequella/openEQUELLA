package com.tle.web.itemlist.item;

import com.tle.beans.item.Item;

public interface ItemListEntryFactoryExtension
{
	StandardItemListEntry createItemListEntry(Item item);
}
