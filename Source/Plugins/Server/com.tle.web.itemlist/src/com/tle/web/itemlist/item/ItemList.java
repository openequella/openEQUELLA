package com.tle.web.itemlist.item;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.web.sections.TreeIndexed;

@NonNullByDefault
@TreeIndexed
public interface ItemList<LE extends ItemListEntry> extends ItemlikeList<Item, LE>
{
	// FIXME: do the needful
}
