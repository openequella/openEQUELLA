package com.tle.core.item;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.core.item.operations.ItemOperationParams;

/**
 * @author aholland
 */
public interface ItemIdExtension
{
	void setup(ItemKey itemId, ItemOperationParams params, Item item);
}
