package com.tle.common;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;

/**
 * @author aholland
 */
public interface ItemIdExtension
{
	void setup(ItemKey itemId, WorkflowOperationParams params, Item item);
}
