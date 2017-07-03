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

package com.tle.core.replicatedcache.dao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.Institution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.common.institution.CurrentInstitution;

@Bind(ReplicatedCacheDao.class)
@Singleton
@SuppressWarnings("nls")
public class ReplicatedCacheDaoImpl extends GenericDaoImpl<CachedValue, Long> implements ReplicatedCacheDao
{
	public ReplicatedCacheDaoImpl()
	{
		super(CachedValue.class);
	}

	@Override
	@Transactional
	public CachedValue get(final String cacheId, final String key)
	{
		return (CachedValue) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session.createQuery("FROM CachedValue WHERE key = :key"
					+ " AND cacheId = :cacheId AND institution = :institution");
				q.setParameter("key", key);
				q.setParameter("cacheId", cacheId);
				q.setParameter("institution", CurrentInstitution.get());

				return q.uniqueResult();
			}
		});
	}

	@Override
	@Transactional
	public void put(final String cacheId, final String key, final Date ttl, final byte[] value)
	{
		invalidate(cacheId, key);

		CachedValue cv = new CachedValue();
		cv.setInstitution(CurrentInstitution.get());
		cv.setCacheId(cacheId);
		cv.setKey(key);
		cv.setValue(value);
		cv.setTtl(ttl);

		save(cv);
	}

	@Override
	@Transactional
	public void invalidateExpiredEntries()
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				// Delete all the things
				Query q = session.createQuery("DELETE FROM CachedValue WHERE ttl < :ttl");
				q.setParameter("ttl", new Date());
				q.executeUpdate();

				return null;
			}
		});
	}

	@Override
	@Transactional
	public void invalidate(final String cacheId, final String... keys)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session.createQuery("DELETE FROM CachedValue WHERE cacheId = :cacheId"
					+ " AND institution = :institution AND key IN (:keys)");
				q.setParameterList("keys", Arrays.asList(keys));
				q.setParameter("cacheId", cacheId);
				q.setParameter("institution", CurrentInstitution.get());

				q.executeUpdate();
				return null;
			}
		});
	}

	@Override
	@Transactional
	public void invalidateAllForInstitution(final Institution inst)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session.createQuery("DELETE FROM CachedValue WHERE institution = :institution");
				q.setParameter("institution", inst);
				q.executeUpdate();
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<CachedValue> getBatch(final String cacheId, final String keyPrefixFilter, final long startId,
		final int batchSize)
	{
		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session.createQuery("FROM CachedValue WHERE cacheId = :cacheId"
					+ " AND institution = :institution AND id > :startId"
					+ " AND (:keyPrefixFilter = '' OR key LIKE :keyPrefixFilter) ORDER BY id ASC");
				q.setParameter("cacheId", cacheId);
				q.setParameter("keyPrefixFilter", keyPrefixFilter + '%');
				q.setParameter("startId", startId);
				q.setParameter("institution", CurrentInstitution.get());
				q.setMaxResults(batchSize);

				return q.list();
			}
		});
	}

}
