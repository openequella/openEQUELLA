package com.tle.core.entity.dao;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.EntityLock;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author Nicholas Read
 */
public interface EntityLockingDao extends GenericDao<EntityLock, String>
{
	void deleteForEntity(BaseEntity entity);

	void deleteAll();
}
