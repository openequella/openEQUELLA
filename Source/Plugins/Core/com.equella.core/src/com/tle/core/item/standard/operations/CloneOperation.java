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

import java.util.UUID;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author aholland
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureOnCall(priv = "CLONE_ITEM")
@SecureItemStatus(value = {ItemStatus.PERSONAL}, not = true)
@SuppressWarnings("nls")
public final class CloneOperation extends AbstractCloneOperation // NOSONAR
{
	@Inject
	private SchemaService schemaService;
	@Inject
	private ItemHelper itemHelper;

	private String oldItemdefUuid;
	private final String newItemdefUuid;
	private final boolean submit;

	/**
	 * Clone in the same item definition.
	 */
	@AssistedInject
	private CloneOperation(@Assisted("copyAttachments") boolean copyAttachments, @Assisted("submit") boolean submit)
	{
		this(null, copyAttachments, submit);
	}

	/**
	 * Clone to possibly a new item definition.
	 * 
	 * @param newItemDefUuid null indicates that it is in the same collection.
	 */
	@AssistedInject
	private CloneOperation(@Assisted String newItemDefUuid, @Assisted("copyAttachments") boolean copyAttachments,
		@Assisted("submit") boolean submit)
	{
		super(copyAttachments);
		this.newItemdefUuid = newItemDefUuid;
		this.submit = submit;
	}

	@Override
	protected Item initItemUuidAndVersion(Item newItem, Item oldItem)
	{
		newItem.setUuid(UUID.randomUUID().toString());
		newItem.setVersion(1);
		return newItem;
	}

	@Override
	protected void pushCloneData(Item item, CloningHelper forCloning)
	{
		super.pushCloneData(item, forCloning);

		if( newItemdefUuid != null )
		{
			oldItemdefUuid = item.getItemDefinition().getUuid();
			item.setItemDefinition(itemdefService.getByUuid(newItemdefUuid));
		}
	}

	@Override
	protected void finalProcessing(Item origItem, Item item)
	{
		super.finalProcessing(origItem, item);

		if( !copyAttachments )
		{
			fileSystemService.removeFile(getStaging(), "");
			// Make a blank folder
			fileSystemService.mkdir(getStaging(), "");
		}

		// now use the transform, if any
		if( !Check.isEmpty(transform) )
		{
			ItemPack<Item> pack = params.getItemPack();

			Item oldItem = pack.getOriginalItem();
			ItemPack<Item> oldPack = new ItemPack<>(oldItem, itemService.getItemXmlPropBag(oldItem), null);
			PropBagEx oldXml = itemHelper.convertToXml(oldPack, new ItemHelperSettings(true));

			try
			{
				Item newItem = pack.getItem();
				PropBagEx newXml = new PropBagEx(schemaService
					.transformForImport(newItem.getItemDefinition().getSchema().getId(), transform, oldXml));
				pack.setXml(newXml);
			}
			catch( Exception ex )
			{
				throw new WorkflowException(
					CurrentLocale.get("com.tle.core.workflow.operations.clone.error.transforming"), ex);
			}
		}
		if( submit )
		{
			params.addOperation(operationFactory.submit());
		}
	}

	@Override
	protected void doHistory()
	{
		// if new itemdef is not the same as old, then technically a move was
		// performed
		if( newItemdefUuid != null && !newItemdefUuid.equals(oldItemdefUuid) )
		{
			createHistory(HistoryEvent.Type.changeCollection);
		}
		else
		{
			createHistory(HistoryEvent.Type.clone);
		}
	}
}
