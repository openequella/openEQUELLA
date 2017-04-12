package com.tle.core.dao;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemLock;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author Nicholas Read
 */
public interface ItemLockingDao extends GenericDao<ItemLock, String>
{
	void deleteForItem(Item entity);

	void deleteAll();
}
