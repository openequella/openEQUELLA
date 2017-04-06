package com.tle.core.workflow.filters;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;

public interface FilterResultListener
{
	void succeeded(ItemKey itemId, ItemPack<Item> pack);

	void failed(ItemKey itemId, Item item, Throwable e);

	void total(int total);
}