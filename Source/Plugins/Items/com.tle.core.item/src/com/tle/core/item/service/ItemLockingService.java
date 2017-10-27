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

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemLock;

public interface ItemLockingService
{
	ItemLock lock(ItemKey itemId);

	ItemLock lock(Item item);

	ItemLock get(ItemKey itemId);

	ItemLock get(Item item);

	boolean isLocked(Item item);

	ItemLock useLock(Item item, String lockId);

	void unlock(ItemLock itemLock);

	void unlock(ItemKey itemId);

	void unlock(Item item, boolean force);
}
