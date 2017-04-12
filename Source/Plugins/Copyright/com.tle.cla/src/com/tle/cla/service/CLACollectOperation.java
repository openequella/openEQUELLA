package com.tle.cla.service;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

@Bind
public class CLACollectOperation extends AbstractWorkflowOperation
{
	@Inject
	private CLAMetadataCollection metadataCollection;

	@Override
	public boolean execute()
	{
		PropBagEx itemXml = getItemXml();
		Item item = getItem();
		metadataCollection.metadataChanged(item, itemXml);
		return false;
	}

}
