package com.tle.core.dao.impl;

import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.EntityLock;
import com.tle.core.dao.EntityLockingDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Nicholas Read
 */
@Bind(EntityLockingDao.class)
@Singleton
@SuppressWarnings("nls")
public class EntityLockingDaoImpl extends GenericDaoImpl<EntityLock, String> implements EntityLockingDao
{
	public EntityLockingDaoImpl()
	{
		super(EntityLock.class);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteForEntity(BaseEntity entity)
	{
		getHibernateTemplate().bulkUpdate("DELETE FROM EntityLock WHERE entity = ?", entity);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAll()
	{
		getHibernateTemplate().bulkUpdate("DELETE FROM ItemLock WHERE institution = ?", CurrentInstitution.get());
	}
}
