package com.tle.mycontent.workflow.operations;

import com.tle.beans.item.ItemStatus;
import com.tle.core.item.standard.operations.workflow.StatusOperation;

public class MyContentStatusOperation extends StatusOperation
{
	@Override
	public ItemStatus getItemStatus()
	{
		return ItemStatus.PERSONAL;
	}
}
