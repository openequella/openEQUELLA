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

package com.tle.core.item.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.google.common.collect.Multimap;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemSelect;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Triple;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.scripting.ScriptEvaluator;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.item.operations.FilterResultListener;
import com.tle.core.item.operations.ItemOperationFilter;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.scripting.WorkflowScriptObjectContributor;
import com.tle.core.remoting.RemoteItemService;

/**
 * @author Nicholas Read
 */
@NonNullByDefault
public interface ItemService extends RemoteItemService, ScriptEvaluator, WorkflowScriptObjectContributor
{
	Item getUnsecure(ItemKey itemId) throws ItemNotFoundException;

	Item getUnsecureIfExists(ItemKey itemId);

	List<ItemId> getItemsWithUrl(String url, ItemDefinition itemDefinition, String excludedUuid);

	List<Item> getVersionDetails(String uuid);

	List<Item> getNextLiveItems(List<ItemId> items);

	/**
	 * @param itemId
	 * @param uuid
	 * @return Never returns null.
	 * @throws AttachmentNotFoundException
	 */
	Attachment getAttachmentForUuid(ItemKey itemId, String uuid);

	Multimap<Item, Attachment> getAttachmentsForItems(Collection<Item> items);

	int getLatestVersion(String uuid);

	Item getLatestVersionOfItem(String uuid);

	int getLiveItemVersion(String uuid);

	ItemIdKey getLiveItemVersionId(String uuid);

	ItemPack<Item> getItemPack(ItemKey key);

	PropBagEx getItemXmlPropBag(Item item);

	/**
	 * Use the other version if possible
	 * 
	 * @param item
	 * @return
	 */
	PropBagEx getItemXmlPropBag(ItemKey key);

	List<Item> queryItems(List<Long> itemkeys);

	List<Item> queryItems(List<ItemIdKey> itemkeys, ItemSelect select);

	List<Item> queryItemsByUuids(List<String> uuids);

	<A> List<A> queryItems(DetachedCriteria criteria, Integer firstResult, Integer maxResults);

	void updateIndexTimes(String whereClause, String[] names, Object[] values);

	List<ItemId> enumerateItems(String whereClause, String[] names, Object[] values);

	List<ItemIdKey> getItemIdKeys(List<Long> ids);

	ItemIdKey getItemIdKey(Long id);

	/**
	 * Returns uuid, version, name
	 */
	List<Triple<String, Integer, String>> enumerateItemNames(String whereClause, String[] names, Object[] values);

	/* LEGACY or not.. */

	void forceUnlock(Item item);

	Map<ItemId, LanguageBundle> getItemNames(Collection<? extends ItemKey> keys);

	Map<ItemId, Long> getItemNameIds(Collection<? extends ItemKey> keys);

	Set<String> getCachedPrivileges(ItemKey itemKey);

	Set<String> getItemPrivsFast(ItemKey itemId);

	Set<String> getReferencedUsers();

	void delete(Item item);

	/**
	 * Filters out item UUIDs from the given list for items that are not in any
	 * of the given collections. Does not modify the passed in item UUID list.
	 * The returned set is not guaranteed to have the contents the same
	 * ordering.
	 */
	Set<String> unionItemUuidsWithCollectionUuids(Collection<String> itemUuids, Set<String> collectionUuids);

	Map<String, Object> getItemInfo(ItemId id);

	Map<ItemId, Item> queryItemsByItemIds(List<? extends ItemKey> itemIds);

	Item getForEdit(ItemKey itemId);

	Item getForNewVersion(ItemKey itemId);

	Attachment getAttachmentForFilepath(ItemKey itemId, String filepath);

	List<String> getNavReferencedAttachmentUuids(List<Item> items);

	Item getItemWithViewAttachmentPriv(ItemKey key);

	/*
	 * OPERATIONS
	 */

	ItemPack<Item> operation(ItemKey key, WorkflowOperation... operations);

	void operateAll(ItemOperationFilter filter);

	void operateAllInTransaction(ItemOperationFilter filter);

	void operateAll(ItemOperationFilter filter, FilterResultListener listener);

	void executeOperationsNow(ItemOperationParams params, Collection<WorkflowOperation> operations);

	List<WorkflowOperation> executeExtensionOperationsNow(ItemOperationParams params, String type);

	List<WorkflowOperation> executeExtensionOperationsLater(ItemOperationParams params, String type);

	boolean isAnOwner(Item item, String userUuid);

	UserBean getOwner(Item item);

	ScriptContext createScriptContext(ItemPack itemPack, FileHandle fileHandle, Map<String, Object> attributes,
		Map<String, Object> objects);

	void updateMetadataBasedSecurity(PropBagEx itemxml, Item item);
}
