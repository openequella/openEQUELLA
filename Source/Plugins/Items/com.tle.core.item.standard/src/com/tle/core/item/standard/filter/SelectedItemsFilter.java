package com.tle.core.item.standard.filter;

import java.util.Collection;
import java.util.Date;

import com.tle.beans.item.ItemId;
import com.tle.core.item.operations.ItemOperationFilter;
import com.tle.core.item.operations.WorkflowOperation;

public class SelectedItemsFilter implements ItemOperationFilter
{
	private final WorkflowOperation[] operations;
	private final Collection<? extends ItemId> keys;

	public SelectedItemsFilter(Collection<? extends ItemId> keys, WorkflowOperation... operations)
	{
		this.keys = keys;
		this.operations = operations;
	}

	@Override
	public WorkflowOperation[] getOperations()
	{
		return operations;
	}

	@Override
	public FilterResults getItemIds()
	{
		return new FilterResults(keys);
	}

	@Override
	public void setDateNow(Date now)
	{
		// nothing
	}

	@Override
	public boolean isReadOnly()
	{
		return false;
	}
}
