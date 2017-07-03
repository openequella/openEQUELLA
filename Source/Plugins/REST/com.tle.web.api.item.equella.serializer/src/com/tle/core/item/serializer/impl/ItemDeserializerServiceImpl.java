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

package com.tle.core.item.serializer.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.ItemEditingException;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.edit.ItemEditorService;
import com.tle.core.item.serializer.ItemDeserializerEditor;
import com.tle.core.item.serializer.ItemDeserializerService;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

@Bind(ItemDeserializerService.class)
@Singleton
@SuppressWarnings("nls")
public class ItemDeserializerServiceImpl implements ItemDeserializerService
{
	@Inject
	private ItemEditorService itemEditorService;
	@Inject
	private PluginTracker<ItemDeserializerEditor> editorsTracker;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemOperationFactory workflowFactory;

	@Override
	@Transactional
	public ItemIdKey edit(EquellaItemBean itemBean, String stagingUuid, String lockId, boolean unlock,
		boolean ensureOnIndexList)
	{
		ItemId itemId = new ItemId(itemBean.getUuid(), itemBean.getVersion());
		ItemEditor editor = itemEditorService.getItemEditor(itemId, stagingUuid, lockId, editorsTracker.getBeanList());
		editor.doEdits(itemBean);
		if( unlock && lockId != null )
		{
			editor.unlock();
		}
		return editor.finishedEditing(ensureOnIndexList);
	}

	@Override
	@Transactional
	public ItemIdKey newItem(EquellaItemBean itemBean, String stagingUuid, boolean dontSubmit,
		boolean ensureOnIndexList, boolean noAutoArchive)
	{
		ItemId itemId = new ItemId(itemBean.getUuid(), itemBean.getVersion());
		BaseEntityReference collectionRef = itemBean.getCollection();
		if( collectionRef == null )
		{
			throw new ItemEditingException("No collection specified");
		}
		String collectionUuid = collectionRef.getUuid();
		ItemEditor editor = itemEditorService.newItemEditor(collectionUuid, itemId, stagingUuid,
			editorsTracker.getBeanList());
		editor.doEdits(itemBean);
		if( !dontSubmit )
		{
			editor.preventSaveScript();
		}
		ItemIdKey itemKey = editor.finishedEditing(ensureOnIndexList);
		if( !dontSubmit )
		{
			itemService.operation(itemKey, workflowFactory.submit(),
				workflowFactory.saveNoIndexing(noAutoArchive, stagingUuid));
		}
		return itemKey;
	}

	@SecureOnCallSystem
	@Transactional
	@Override
	public ItemIdKey importItem(EquellaItemBean itemBean, String stagingUuid, boolean ensureOnIndexList)
	{
		BaseEntityReference collectionRef = itemBean.getCollection();
		if( collectionRef == null )
		{
			throw new ItemEditingException("No collection specified");
		}
		String collectionUuid = collectionRef.getUuid();
		ItemEditor editor = itemEditorService.importItemEditor(collectionUuid,
			new ItemId(itemBean.getUuid(), itemBean.getVersion()), stagingUuid, editorsTracker.getBeanList());
		editor.doEdits(itemBean);

		ItemIdKey itemKey = editor.finishedEditing(ensureOnIndexList);

		return itemKey;
	}
}
