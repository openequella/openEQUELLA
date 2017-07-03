package com.tle.core.item.service.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.LockedException;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemLock;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.item.dao.ItemLockingDao;
import com.tle.core.item.service.ItemLockingService;
import com.tle.core.item.service.ItemService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.exceptions.AccessDeniedException;

@Bind(ItemLockingService.class)
@Singleton
public class ItemLockingServiceImpl implements ItemLockingService
{
	@Inject
	private ItemService itemService;
	@Inject
	private ItemLockingDao dao;

	@Override
	@Transactional
	public ItemLock lock(ItemKey itemId)
	{
		Item item = itemService.getForEdit(itemId);
		return lock(item);
	}

	@Override
	@Transactional
	public ItemLock get(ItemKey itemId)
	{
		Item item = itemService.getForEdit(itemId);
		return get(item);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public boolean isLocked(Item item)
	{
		return get(item) != null;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void unlock(ItemLock itemLock)
	{
		if( itemLock == null )
		{
			throw new AccessDeniedException("Item is not locked");
		}
		unlock(itemLock.getItem(), true);
	}

	@Override
	@Transactional
	public void unlock(ItemKey itemId)
	{
		unlock(get(itemId));
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public ItemLock useLock(Item item, String lockId)
	{
		ItemLock itemLock = get(item);
		if( itemLock == null )
		{
			throw new AccessDeniedException("Item is not locked");
		}
		if( !itemLock.getUserSession().equals(lockId) )
		{
			throw new LockedException("Wrong lock id '" + lockId + "'", itemLock.getUserID(), itemLock.getUserSession(),
				item.getId());
		}
		return itemLock;
	}

	@Override
	@Transactional
	public ItemLock lock(Item item)
	{
		ItemLock lock = dao.findByCriteria(Restrictions.eq("item", item));
		if( lock != null )
		{
			throw new LockedException("Item is already locked", lock.getUserID(), lock.getUserSession(), item.getId());
		}

		UserState userState = CurrentUser.getUserState();

		lock = new ItemLock();
		lock.setItem(item);
		lock.setUserID(userState.getUserBean().getUniqueID());
		lock.setUserSession(userState.getSessionID());
		lock.setInstitution(CurrentInstitution.get());
		dao.save(lock);
		return lock;
	}

	@Override
	@Transactional
	public void unlock(Item item, boolean force)
	{
		ItemLock lock = dao.findByCriteria(Restrictions.eq("item", item));
		if( lock != null )
		{
			if( force || CurrentUser.getSessionID().equals(lock.getUserSession()) )
			{
				dao.delete(lock);
			}
			else
			{
				throw new LockedException("Item is locked by another session", lock.getUserID(), lock.getUserSession(),
					item.getId());
			}
		}
	}

	@Override
	@Transactional
	public ItemLock get(Item item)
	{
		return dao.findByCriteria(Restrictions.eq("item", item));
	}
}
