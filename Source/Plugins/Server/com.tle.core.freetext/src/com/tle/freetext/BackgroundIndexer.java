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

package com.tle.freetext;

import java.util.Collection;
import java.util.Date;

import com.tle.beans.Institution;
import com.tle.beans.item.ItemIdKey;

public interface BackgroundIndexer extends Runnable
{
	IndexedItem createIndexedItem(ItemIdKey key);

	void addToQueue(IndexedItem item);

	void addToQueue(ItemIdKey key, boolean newSearcher);

	void addAllToQueue(Collection<IndexedItem> items);

	void kill();

	IndexedItem getIndexedItem(ItemIdKey key);

	void synchronizeNew(Collection<Institution> institutions, Date since);

	void synchronizeFull(Collection<Institution> institutions);

	boolean isRoomForItems(int size);

}