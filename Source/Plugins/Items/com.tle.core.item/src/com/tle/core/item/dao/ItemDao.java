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

package com.tle.core.item.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dytech.edge.common.valuebean.ItemIndexDate;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.tle.beans.IdCloneable;
import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.DrmAcceptance;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemSelect;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * @author Nicholas Read
 */
public interface ItemDao extends GenericInstitutionalDao<Item, Long>
{
	List<Integer> getCommentCounts(List<Item> items);

	List<Item> getItems(List<Long> keys, ItemSelect select, Institution institution);

	List<Item> getAllVersionsOfItem(String uuid);

	List<Item> getItems(List<String> uuids, Institution institution);

	Map<ItemId, Item> getItems(List<? extends ItemKey> itemIds);

	List<ItemId> getItemsWithUrl(String url, ItemDefinition itemDefinition, String excludedUuid);

	List<ItemId> enumerateItemKeys(String whereClause, String[] names, Object[] values);

	/**
	 * WARNING: Does not filter by institution - that's up to the caller to do.
	 */
	List<ItemIdKey> getItemKeyBatch(String joinClause, String whereClause, Map<String, Object> params, long startId,
		int batchSize);

	long getCount(String joinClause, String whereClause, Map<String, Object> params);

	List<ItemIdKey> getItemIdKeys(List<Long> ids);

	ItemIdKey getItemIdKey(Long id);

	List<Triple<String, Integer, String>> enumerateItemNames(String whereClause, String[] names, Object[] values);

	List<Item> getNextLiveItems(List<ItemId> items);

	Map<ItemId, LanguageBundle> getItemNames(Collection<? extends ItemKey> keys);

	Map<ItemId, Long> getItemNameIds(Collection<? extends ItemKey> keys);

	String getNameForId(long id);

	List<ItemIdKey> listAll(Institution institution);

	List<Integer> getAllVersionNumbers(String uuid);

	int getLatestVersion(String uuid);

	int getLatestLiveVersion(String uuid);

	ItemIdKey getLatestLiveVersionId(String uuid);

	int updateIndexTimes(String whereClause, String[] names, Object[] values);

	Item get(ItemKey id);

	Item get(ItemKey id, boolean readonly);

	Item findByItemId(ItemKey id);

	Set<String> getReferencedUsers();

	void clearHistory(Item item);

	<T extends IdCloneable> T mergeTwo(T oldObject, T newObject);

	long getCollectionIdForUuid(String uuid);

	Attachment getAttachmentByUuid(ItemKey itemId, String uuid);

	Set<String> unionItemUuidsWithCollectionUuids(Collection<String> itemUuids, Set<String> collectionUuids);

	Map<String, Object> getItemInfo(String uuid, int version);

	ListMultimap<Item, Attachment> getAttachmentsForItems(Collection<Item> items);

	ListMultimap<Long, Attachment> getAttachmentsForItemIds(Collection<Long> items);

	ListMultimap<Long, HistoryEvent> getHistoryForItemIds(Collection<Long> items);

	ListMultimap<Long, ItemNavigationNode> getNavigationNodesForItemIds(Collection<Long> items);

	Multimap<Long, String> getCollaboratorsForItemIds(Collection<Long> itemIds);

	ListMultimap<Long, DrmAcceptance> getDrmAcceptancesForItemIds(Collection<Long> itemIds);

	Item getExistingItem(ItemKey itemId);

	Pair<Long, Long> getIdRange(Collection<Institution> institutions, Date afterDate);

	List<ItemIndexDate> getIndexTimesFromId(Collection<Institution> institutions, Date afterDate, long itemId,
		long maxItemId, int maxResults);

	Attachment getAttachmentByFilepath(ItemKey itemId, String filepath);

	List<String> getNavReferencedAttachmentUuids(List<Item> items);
}
