package com.tle.core.workflow.operations;

import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;

/**
 * @author aholland
 */
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

	void setParams(WorkflowParams params);

	boolean failedToAutowire();
}
