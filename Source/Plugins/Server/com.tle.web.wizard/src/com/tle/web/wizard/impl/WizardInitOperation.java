/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.wizard.impl;

import javax.inject.Inject;

import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.workflow.Workflow;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;

@Bind
public class WizardInitOperation extends AbstractStandardWorkflowOperation
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
