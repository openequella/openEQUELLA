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

package com.tle.core.item.edit.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.LockedException;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemEditingException;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemLock;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ItemXml;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.edit.ItemEditorService;
import com.tle.core.item.edit.impl.ItemEditorImpl.ItemEditorFactory;
import com.tle.core.item.serializer.ItemDeserializerEditor;
import com.tle.core.item.service.ItemLockingService;
import com.tle.core.item.service.ItemService;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.common.usermanagement.user.CurrentUser;

@Bind(ItemEditorService.class)
@Singleton
@SuppressWarnings("nls")
public class ItemEditorServiceImpl implements ItemEditorService
{
	@Inject
	private ItemService itemService;
	@Inject
	private ItemLockingService itemLockingService;
	@Inject
	private ItemDao itemDao;
	@Inject
	private ItemEditorFactory editorFactory;
	@Inject
	private ItemDefinitionService collectionService;

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ItemEditor getItemEditor(ItemKey itemKey, String stagingUuid, String lockId,
		List<ItemDeserializerEditor> deserializerEditors)
	{
		Item item = itemDao.getExistingItem(itemKey);
		ItemLock lock = null;
		if( lockId != null )
		{
			lock = itemLockingService.useLock(item, lockId);
		}
		else if( itemLockingService.isLocked(item) )
		{
			throw new LockedException("Item is locked for editing", CurrentUser.getUserID(), CurrentUser.getSessionID(),
				item.getId());
		}
		ItemEditorImpl editor = editorFactory.createExistingEditor(item, lock, deserializerEditors);
		editor.setStagingUuid(stagingUuid);
		return editor;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ItemEditor newItemEditor(String collectionUuid, ItemKey itemKey, String stagingUuid,
		List<ItemDeserializerEditor> deserializerEditors)
	{
		ItemDefinition collection = collectionService.getForItemCreate(collectionUuid);
		String uuid = itemKey.getUuid();
		int version = itemKey.getVersion();
		if( Check.isEmpty(uuid) )
		{
			uuid = UUID.randomUUID().toString();
		}
		else
		{
			ItemEditorImpl.checkValidUuid(uuid);
		}

		List<Integer> existingVersions = itemDao.getAllVersionNumbers(uuid);

		if( !existingVersions.isEmpty() )
		{
			int latestVersion = existingVersions.get(existingVersions.size() - 1);
			if( version != 0 )
			{
				if( existingVersions.contains(version) )
				{
					throw new ItemEditingException("Version " + version + " of the item '" + uuid + "' already exists");
				}
				if( version != latestVersion + 1 )
				{
					throw new ItemEditingException("Item '" + uuid + "' has " + latestVersion
						+ " version(s), you must create version " + (latestVersion + 1) + " or use 0");
				}
			}
			else
			{
				version = latestVersion + 1;
			}
			// Just check NEW_VERSION
			itemService.getForNewVersion(new ItemId(uuid, latestVersion));
		}
		else
		{
			if( version > 1 )
			{
				throw new ItemEditingException(
					"No existing item with uuid '" + uuid + "' exists, must set version to 1 or use 0");
			}
			version = 1;
		}
		Item item = new Item();
		item.setOwner(CurrentUser.getUserID());
		item.setUuid(uuid);
		item.setVersion(version);
		item.setItemDefinition(collection);
		item.setDateCreated(new Date());
		item.setStatus(ItemStatus.DRAFT);
		item.setInstitution(CurrentInstitution.get());
		item.setItemXml(new ItemXml("<xml/>"));
		ItemEditorImpl editor = editorFactory.createNewEditor(item, deserializerEditors);
		editor.setStagingUuid(stagingUuid);
		return editor;
	}

	@SecureOnCallSystem
	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public ItemEditor importItemEditor(String collectionUuid, ItemKey itemKey, String stagingUuid,
		List<ItemDeserializerEditor> deserializerEditors)
	{
		ItemDefinition collection = collectionService.getForItemCreate(collectionUuid);
		String uuid = itemKey.getUuid();
		int version = itemKey.getVersion();
		if( Check.isEmpty(uuid) )
		{
			uuid = UUID.randomUUID().toString();
			version = 1;
		}
		else
		{
			ItemEditorImpl.checkValidUuid(uuid);
		}

		Item item = new Item();
		item.setUuid(uuid);
		item.setVersion(version);
		item.setItemDefinition(collection);
		item.setInstitution(CurrentInstitution.get());
		item.setItemXml(new ItemXml("<xml/>"));
		final ItemEditorImpl editor = editorFactory.createImportEditor(item, deserializerEditors, true);
		editor.setStagingUuid(stagingUuid);
		return editor;
	}
}
