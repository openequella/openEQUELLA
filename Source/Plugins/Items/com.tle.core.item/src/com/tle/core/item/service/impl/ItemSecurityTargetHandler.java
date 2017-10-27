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

package com.tle.core.item.service.impl;

import static com.tle.common.security.SecurityConstants.TARGET_DYNAMIC_ITEM_METADATA;
import static com.tle.common.security.SecurityConstants.TARGET_ITEM;

import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.security.ItemMetadataTarget;
import com.tle.common.security.ItemStatusTarget;
import com.tle.core.guice.Bind;
import com.tle.core.security.SecurityTargetHandler;
import com.tle.core.security.impl.ItemDynamicMetadataTarget;

@Bind
@Singleton
@SuppressWarnings({"nls", "deprecation"})
public class ItemSecurityTargetHandler implements SecurityTargetHandler
{
	@Inject
	private ItemStatusAndMetadataSecurityTargetHandler itemStatusAndMetadataHandler;

	@Override
	public void gatherAllLabels(Set<String> labels, Object target)
	{
		final Item item = (Item) target;
		final ItemDefinition itemDef = item.getItemDefinition();

		labels.add(getPrimaryLabel(target));
		labels.add(getDynamicItemMetadataLabel(item));

		itemStatusAndMetadataHandler.gatherAllLabels(labels, new ItemStatusTarget(item.getStatus(), itemDef));

		if( item.getMetadataSecurityTargets() != null )
		{
			for( String extra : item.getMetadataSecurityTargets() )
			{
				itemStatusAndMetadataHandler.gatherAllLabels(labels, new ItemMetadataTarget(extra, itemDef));
			}
		}
	}

	private String getDynamicItemMetadataLabel(Item item)
	{
		return TARGET_DYNAMIC_ITEM_METADATA + ":" + item.getId() + ":" + item.getItemDefinition().getId();
	}

	@Override
	public String getPrimaryLabel(Object target)
	{
		if( target instanceof Item )
		{
			return TARGET_ITEM + ":" + ((Item) target).getId();
		}
		ItemDynamicMetadataTarget idt = (ItemDynamicMetadataTarget) target;
		return getDynamicItemMetadataLabel(idt.getItem());
	}

	@Override
	public Object transform(Object target)
	{
		return ((ItemPack) target).getItem();
	}

	@Override
	public boolean isOwner(Object target, String userId)
	{
		Item item = (Item) target;
		if( Objects.equals(item.getOwner(), userId) )
		{
			return true;
		}
		else if( item.getCollaborators() != null )
		{
			return item.getCollaborators().contains(userId);
		}
		return false;
	}

}
