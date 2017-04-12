package com.tle.core.workflow.operations;

import com.tle.beans.item.ItemStatus;

public class MyContentStatusOperation extends StatusOperation
{
	@Override
	public ItemStatus getItemStatus()
	{
		return ItemStatus.PERSONAL;
	}
}
