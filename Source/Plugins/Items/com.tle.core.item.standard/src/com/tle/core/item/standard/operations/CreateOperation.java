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

package com.tle.core.item.standard.operations;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.item.helper.ItemHelper;
import com.tle.common.usermanagement.user.CurrentUser;

public class CreateOperation extends AbstractStandardWorkflowOperation
{
	private final PropBagEx initialXml;
	private final ItemDefinition itemDef;
	private final ItemStatus status;
	private StagingFile staging;

	@Inject
	private StagingService stagingService;
	@Inject
	private ItemHelper itemHelper;

	@AssistedInject
	protected CreateOperation()
	{
		this(null, null, null, ItemStatus.DRAFT);
	}

	@AssistedInject
	protected CreateOperation(@Assisted ItemDefinition itemDef)
	{
		this(null, itemDef, null, ItemStatus.DRAFT);
	}

	@AssistedInject
	protected CreateOperation(@Assisted ItemDefinition itemDef, @Assisted StagingFile staging)
	{
		this(null, itemDef, staging, ItemStatus.DRAFT);
	}

	@AssistedInject
	protected CreateOperation(@Assisted ItemDefinition itemDef, @Assisted ItemStatus status)
	{
		this(null, itemDef, null, status);
	}

	@AssistedInject
	protected CreateOperation(@Assisted @Nullable PropBagEx initialXml, @Assisted ItemDefinition itemDef,
		@Nullable @Assisted StagingFile staging)
	{
		this(initialXml, itemDef, staging, ItemStatus.DRAFT);
	}

	private CreateOperation(PropBagEx initialXml, ItemDefinition itemDef, StagingFile staging, ItemStatus status)
	{
		this.initialXml = initialXml;
		this.itemDef = itemDef;
		this.staging = staging;
		this.status = status;
	}

	@Override
	public boolean execute()
	{
		if( staging == null )
		{
			staging = stagingService.createStagingArea();
		}

		final ItemPack<Item> pack;
		if( initialXml != null )
		{
			pack = itemHelper.convertToItemPack(initialXml);
			pack.setStagingID(staging.getUuid());
		}
		else
		{
			pack = new ItemPack<>(new Item(), new PropBagEx(), staging.getUuid());
		}

		final Item itemBean = pack.getItem();
		itemBean.setOwner(CurrentUser.getUserID());
		if( Check.isEmpty(itemBean.getUuid()) )
		{
			itemBean.setUuid(UUID.randomUUID().toString());
		}
		itemBean.setVersion(1);
		itemBean.setNewItem(true);
		itemBean.setStatus(status);
		itemBean.setItemDefinition(itemDef);
		itemBean.setDateCreated(new Date());

		params.setItemPack(pack);
		params.setItemKey(new ItemId(itemBean.getUuid(), 1), 0l);
		params.setUpdateSecurity(true);
		return true;
	}

	public void setStagingService(StagingService stagingService)
	{
		this.stagingService = stagingService;
	}
}
