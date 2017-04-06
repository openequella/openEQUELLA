package com.tle.core.dao.impl;

import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemLock;
import com.tle.core.dao.ItemLockingDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Nicholas Read
 */
@Bind(ItemLockingDao.class)
@Singleton
@SuppressWarnings("nls")
public class ItemLockingDaoImpl extends GenericDaoImpl<ItemLock, String> implements ItemLockingDao
{
	public ItemLockingDaoImpl()
	{
		super(ItemLock.class);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteForItem(Item item)
	{
		getHibernateTemplate().bulkUpdate("delete from ItemLock where item = ?", item);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAll()
	{
		getHibernateTemplate().bulkUpdate("delete from ItemLock where institution = ?", CurrentInstitution.get());
	}
}
