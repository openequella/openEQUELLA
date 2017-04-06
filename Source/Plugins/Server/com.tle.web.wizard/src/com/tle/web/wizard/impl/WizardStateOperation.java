package com.tle.web.wizard.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.web.wizard.WizardState;

public class WizardStateOperation extends AbstractWorkflowOperation
{
	private WizardState state;

	@AssistedInject
	protected WizardStateOperation(@Assisted WizardState state)
	{
		this.state = state;
	}

	@Override
	public boolean execute()
	{
		Item item = state.getItem();
		params.setItemKey(new ItemId(item.getUuid(), item.getVersion()), 0l);
		params.setItemPack(new ItemPack(item, state.getItemxml(), state.getStagingId()));
		params.setUpdateSecurity(true);
		return false;
	}
}
