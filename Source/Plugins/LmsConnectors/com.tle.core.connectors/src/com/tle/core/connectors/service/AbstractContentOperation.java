package com.tle.core.connectors.service;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.core.workflow.operations.WorkflowParams;

public abstract class AbstractContentOperation implements WorkflowOperation
{
	@Inject
	private ConnectorRepositoryService repositoryService;

	protected WorkflowParams params;

	@Override
	public Item getItem()
	{
		return null;
	}

	@Override
	public ItemPack getItemPack()
	{
		return null;
	}

	@Override
	public boolean isReadOnly()
	{
		return true;
	}

	@Override
	public void setParams(WorkflowParams params)
	{
		this.params = params;
	}

	@Override
	public boolean failedToAutowire()
	{
		return repositoryService == null;
	}

}
