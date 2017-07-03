package com.tle.core.item.operations;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;

/**
 * @author aholland
 */
@NonNullByDefault
public interface WorkflowOperation
{
	/**
	 * @return true if you modified the item
	 */
	boolean execute();

	@Nullable
	Item getItem();

	@Nullable
	ItemPack<Item> getItemPack();

	boolean isReadOnly();

	void setParams(ItemOperationParams params);

	boolean failedToAutowire();

	/**
	 * I.e. purge or delete
	 * @return
	 */
	boolean isDeleteLike();
}
