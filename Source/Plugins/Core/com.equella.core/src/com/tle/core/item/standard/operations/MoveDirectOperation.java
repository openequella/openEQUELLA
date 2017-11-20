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

import java.util.Objects;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.common.workflow.Workflow;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.item.event.ItemMovedCollectionEvent;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author aholland
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureOnCall(priv = SecurityConstants.MOVE_ITEM)
@SecureItemStatus(value = {ItemStatus.PERSONAL}, not = true)
public final class MoveDirectOperation extends AbstractStandardWorkflowOperation
	implements
		MetadataTransformingOperation // NOSONAR
{
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private SchemaService schemaService;
	@Inject
	private ItemOperationFactory workflowFactory;

	private final String newItemdefUuid;
	private final boolean copyFiles;

	private String transform;
	/**
	 * If forWizard == true then it's only generating the wizard item, not persisting to DB
	 */
	private boolean forWizard;

	@AssistedInject
	private MoveDirectOperation(@Assisted final String newItemdefUuid, @Assisted final boolean copyFiles)
	{
		this.newItemdefUuid = newItemdefUuid;
		this.copyFiles = copyFiles;
	}

	@Override
	public void setTransform(final String transform)
	{
		this.transform = transform;
	}

	@Override
	public boolean execute()
	{
		final Item item = getItem();
		final ItemDefinition oldItemdef = item.getItemDefinition();
		final ItemDefinition newItemdef = itemdefService.getByUuid(newItemdefUuid);

		if( !forWizard && item.isModerating() )
		{
			Workflow oldWorkflow = oldItemdef.getWorkflow();
			Workflow newWorkflow = newItemdef.getWorkflow();
			if( workflowId(oldWorkflow) != workflowId(newWorkflow) )
			{
				params.addOperation(workflowFactory.reset());
			}
		}
		// use the transform, if any
		if( !Check.isEmpty(transform) )
		{
			final ItemPack oldPack = new ItemPack(item, itemService.getItemXmlPropBag(item), null);
			final PropBagEx oldXml = itemHelper.convertToXml(oldPack, new ItemHelperSettings(true));

			try
			{
				final PropBagEx newXml = new PropBagEx(
					schemaService.transformForImport(newItemdef.getSchema().getId(), transform, oldXml));
				getItemPack().setXml(newXml);
			}
			catch( final Exception p )
			{
				throw new WorkflowException(
					CurrentLocale.get("com.tle.core.workflow.operations.clone.error.transforming"), p); //$NON-NLS-1$
			}
		}
		item.setItemDefinition(newItemdef);
		params.setUpdateSecurity(true);
		createHistory(HistoryEvent.Type.changeCollection);

		//copy files (for bulk op purposes)
		if( copyFiles )
		{
			copyFiles(item, oldItemdef, newItemdef);
		}

		if( !forWizard )
		{
			addAfterCommitEvent(new ItemMovedCollectionEvent(item.getItemId(), oldItemdef.getUuid(), newItemdefUuid));
		}

		return true;
	}

	private void copyFiles(Item item, ItemDefinition oldCollection, ItemDefinition newCollection)
	{
		final String oldCollectionUuid = oldCollection.getUuid();
		final String newCollectionUuid = newCollection.getUuid();
		if( !oldCollectionUuid.equals(newCollectionUuid) )
		{
			final String oldFilestoreId = oldCollection
				.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_FILESTORE);
			final String newFilestoreId = newCollection
				.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_FILESTORE);
			final boolean oldBucket = oldCollection.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_BUCKETS,
				false);
			final boolean newBucket = newCollection.getAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_BUCKETS,
				false);
			if( !Objects.equals(oldFilestoreId, newFilestoreId) || oldBucket != newBucket )
			{
				final ItemKey itemId = item.getItemId();
				final ItemFile oldHandle = new ItemFile(itemId.getUuid(), itemId.getVersion(),
					oldBucket ? oldCollectionUuid : null);
				oldHandle.setFilestoreId("default".equals(oldFilestoreId) ? null : oldFilestoreId);
				final ItemFile newHandle = new ItemFile(itemId.getUuid(), itemId.getVersion(),
					newBucket ? newCollectionUuid : null);
				newHandle.setFilestoreId("default".equals(newFilestoreId) ? null : newFilestoreId);

				fileSystemService.copy(oldHandle, newHandle);
			}
		}
	}

	private long workflowId(Workflow workflow)
	{
		if( workflow == null )
		{
			return 0;
		}
		return workflow.getId();
	}

	public void setForWizard(boolean forWizard)
	{
		this.forWizard = forWizard;
	}
}
