package com.tle.web.itemlist.item;

import com.tle.beans.item.Item;

/**
 * @author Aaron
 */
public interface ItemListEntry extends ItemlikeListEntry<Item>
{
	int getRating();
}