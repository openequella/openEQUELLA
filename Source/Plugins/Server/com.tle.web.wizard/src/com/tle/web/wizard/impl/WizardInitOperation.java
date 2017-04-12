package com.tle.web.wizard.impl;

import javax.inject.Inject;

import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.workflow.Workflow;
import com.tle.core.guice.Bind;
import com.tle.core.services.InitialiserService;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

@Bind
public class WizardInitOperation extends AbstractWorkflowOperation
{
	@Inject
	private InitialiserService initialiserService;

	@Override
	public boolean execute()
	{
		ItemPack<Item> pack = params.getItemPack();
		Item item = pack.getItem();
		ItemDefinition collection = item.getItemDefinition();
		Schema schema = initialiserService.initialise(collection.getSchema());
		Workflow workflow = initialiserService.initialise(collection.getWorkflow());
		collection = initialiserService.initialise(collection);
		item = initialiserService.initialise(item);
		item.setItemDefinition(collection);
		collection.setSchema(schema);
		collection.setWorkflow(workflow);
		ItemPack<Item> itemPack = new ItemPack<>();
		itemPack.setItem(item);
		itemPack.setXml(pack.getXml());
		itemPack.setStagingID(pack.getStagingID());
		itemPack.setOriginalItem(pack.getOriginalItem());
		params.setItemPack(itemPack);
		return false;
	}
}
