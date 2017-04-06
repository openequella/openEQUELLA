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
