package com.tle.core.services;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.EntityLock;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemLock;

/**
 * @author Nicholas Read
 */
public interface LockingService
{
	EntityLock lockEntity(BaseEntity entity);

	EntityLock lockEntity(BaseEntity entity, String sessionId);

	void unlockEntity(BaseEntity entity, boolean force);

	EntityLock getLock(BaseEntity entity);

	EntityLock getLock(BaseEntity entity, String lockId);

	/**
	 * Does not attempt to perform a lock or see if we own it the existing one,
	 * just gets an existing lock (if any)
	 * 
	 * @param entity
	 * @return
	 */
	EntityLock getLockUnbound(BaseEntity entity);

	/**
	 * @param entity
	 * @param userId Optional. Is the entity locked by the supplied user ID?
	 * @param sessionId Optional. Is the entity locked by the supplied session
	 *            ID?
	 * @return
	 */
	boolean isEntityLocked(BaseEntity entity, String userId, String sessionId);

	ItemLock lockItem(Item item);

	void unlockItem(Item item, boolean force);

	ItemLock getLock(Item item);
}
