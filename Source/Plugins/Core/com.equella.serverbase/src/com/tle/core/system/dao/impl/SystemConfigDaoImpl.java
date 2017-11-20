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

package com.tle.core.system.dao.impl;

import javax.inject.Singleton;

import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.SystemDatabase;
import com.tle.core.migration.beans.SystemConfig;
import com.tle.core.system.dao.SystemConfigDao;
import com.tle.core.system.impl.AbstractSystemDaoImpl;

@Singleton
@SystemDatabase
@SuppressWarnings("nls")
@Bind(SystemConfigDao.class)
public class SystemConfigDaoImpl extends AbstractSystemDaoImpl<SystemConfig, String> implements SystemConfigDao
{
	public SystemConfigDaoImpl()
	{
		super(SystemConfig.class);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public String getConfig(final String key)
	{
		return (String) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				return session.createQuery("SELECT value FROM SystemConfig WHERE key = :key").setParameter("key", key)
					.uniqueResult();
			}
		});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void updateConfig(String key, String value)
	{
		SystemConfig sc = new SystemConfig();
		sc.setKey(key);
		sc.setValue(value);
		getHibernateTemplate().saveOrUpdate(sc);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public long getAndIncrement(final String key)
	{
		return (Long) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("FROM SystemConfig sc WHERE sc.key = :key").setParameter("key", key)
					.setLockMode("sc", LockMode.UPGRADE_NOWAIT);
				while( true )
				{
					try
					{
						SystemConfig sc = (SystemConfig) query.uniqueResult();
						Long v = Long.valueOf(sc.getValue());
						sc.setValue(Long.toString(v.longValue() + 1));
						session.update(sc);
						session.flush();

						return v;
					}
					catch( HibernateOptimisticLockingFailureException e )
					{
						// Sleep for a little bit and keep trying
						try
						{
							Thread.sleep(50);
						}
						catch( InterruptedException e1 )
						{
							// Ignore
						}
					}
				}
			}
		});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void increaseToAtLeast(final String key, final long value)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{

				session
					.createQuery(
						"UPDATE SystemConfig sc SET sc.value = :value WHERE sc.key = :key AND sc.value < :value")
					.setParameter("key", key).setParameter("value", value).executeUpdate();
				session.refresh(new SystemConfig(key));
				return null;
			}
		});
	}
}
