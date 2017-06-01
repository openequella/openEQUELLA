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

package com.tle.core.services.item.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.LockedException;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemLock;
import com.tle.core.guice.Bind;
import com.tle.core.services.LockingService;
import com.tle.core.services.item.ItemLockingService;
import com.tle.core.services.item.ItemService;
import com.tle.exceptions.AccessDeniedException;

@Bind(ItemLockingService.class)
@Singleton
public class ItemLockingServiceImpl implements ItemLockingService
{
	@Inject
	private ItemService itemService;
	@Inject
	private LockingService lockingService;

	@Override
	@Transactional
	public ItemLock lock(ItemKey itemId)
	{
		Item item = itemService.getForEdit(itemId);
		return lockingService.lockItem(item);
	}

	@Override
	@Transactional
	public ItemLock get(ItemKey itemId)
	{
		Item item = itemService.getForEdit(itemId);
		return lockingService.getLock(item);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public boolean isLocked(Item item)
	{
		return lockingService.getLock(item) != null;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void unlock(ItemLock itemLock)
	{
		if( itemLock == null )
		{
			throw new AccessDeniedException("Item is not locked");
		}
		lockingService.unlockItem(itemLock.getItem(), true);
	}

	@Override
	@Transactional
	public void unlock(ItemKey itemId)
	{
		unlock(get(itemId));
	}

	@SuppressWarnings("nls")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ItemLock useLock(Item item, String lockId)
	{
		ItemLock itemLock = lockingService.getLock(item);
		if( itemLock == null )
		{
			throw new AccessDeniedException("Item is not locked");
		}
		if( !itemLock.getUserSession().equals(lockId) )
		{
			throw new LockedException("Wrong lock id '" + lockId + "'", itemLock.getUserID(),
				itemLock.getUserSession(), item.getId());
		}
		return itemLock;
	}
}
