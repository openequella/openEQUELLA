package com.tle.core.item.service;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.core.filesystem.ItemFile;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public interface ItemFileService
{
	ItemFile getItemFile(String uuid, int version, @Nullable ItemDefinition collection);

	ItemFile getItemFile(ItemKey itemId, @Nullable ItemDefinition collection);

	ItemFile getItemFile(Item item);
}