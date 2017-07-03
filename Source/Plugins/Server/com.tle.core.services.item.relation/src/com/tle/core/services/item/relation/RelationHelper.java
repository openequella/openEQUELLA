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

package com.tle.core.services.item.relation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.Relation;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.AbstractHelper;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.operations.SaveOperation;
import com.tle.core.services.item.relation.RelationOperation.RelationOperationFactory;

@Bind
@Singleton
public class RelationHelper extends AbstractHelper
{
	@Inject
	private RelationService relationService;
	@Inject
	private ItemService itemService;
	@Inject
	private RelationOperationFactory relationOperationFactory;

	@Override
	public void load(PropBagEx item, Item bean)
	{
		if( !bean.isNewItem() )
		{
			loadRelations(item, relationService.getAllByFromItem(bean), true);
			loadRelations(item, relationService.getAllByToItem(bean), false);
		}
	}

	private void loadRelations(PropBagEx itemXml, Collection<Relation> relations, boolean from)
	{
		if( Check.isEmpty(relations) )
		{
			return;
		}

		PropBagEx relsXml = itemXml.aquireSubtree("relations").newSubtree( //$NON-NLS-1$
			from ? "targets" : "sources"); //$NON-NLS-1$//$NON-NLS-2$

		for( Relation relation : relations )
		{
			String myRes = from ? relation.getFirstResource() : relation.getSecondResource();
			Item otherItem = from ? relation.getSecondItem() : relation.getFirstItem();
			String otherRes = from ? relation.getSecondResource() : relation.getFirstResource();

			PropBagEx relXml = relsXml.newSubtree("relation"); //$NON-NLS-1$
			relXml.setNode("@type", relation.getRelationType()); //$NON-NLS-1$
			relXml.setIfNotEmpty("@resource", myRes); //$NON-NLS-1$

			PropBagEx otherItemXml = relXml.newSubtree("item"); //$NON-NLS-1$
			otherItemXml.setNode("@uuid", otherItem.getUuid()); //$NON-NLS-1$
			otherItemXml.setNode("@version", otherItem.getVersion()); //$NON-NLS-1$
			otherItemXml.setIfNotEmpty("@resource", otherRes); //$NON-NLS-1$
			otherItemXml.setIfNotEmpty("name", CurrentLocale.get(otherItem.getName(), null)); //$NON-NLS-1$
			otherItemXml.setIfNotEmpty("description", CurrentLocale.get(otherItem.getDescription(), null)); //$NON-NLS-1$
		}
	}

	@Override
	public void save(PropBagEx xml, ItemPack<Item> pack, Set<String> handled)
	{
		Item item = pack.getItem();
		RelationOperationState state = new RelationOperationState();
		if( !item.isNewItem() )
		{
			Item realItem = itemService.get(item.getItemId());
			Collection<Relation> allCurrent = relationService.getAllByFromItem(realItem);
			state.initForCurrent(allCurrent);
		}
		state.deleteAll();

		for( PropBagEx relationXml : xml.iterateAll("relations/targets/relation") ) //$NON-NLS-1$
		{
			String type = relationXml.getNode("@type"); //$NON-NLS-1$
			String resourceId = relationXml.getNode("@resource"); //$NON-NLS-1$
			String uuid = relationXml.getNode("item/@uuid"); //$NON-NLS-1$
			int version = relationXml.getIntNode("item/@version"); //$NON-NLS-1$
			state.add(new ItemId(uuid, version), type, resourceId);
		}

		List<WorkflowOperation> preSave = pack.getAttribute(SaveOperation.KEY_PRESAVE);
		if( preSave == null )
		{
			preSave = new ArrayList<>();
			pack.setAttribute(SaveOperation.KEY_PRESAVE, preSave);
		}
		preSave.add(relationOperationFactory.create(state));

		handled.add("relations"); //$NON-NLS-1$
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		throw new Error();
	}
}
