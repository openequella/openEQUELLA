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

package com.tle.core.entity.dao.impl;

import java.util.*;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.entity.EnumerateOptions;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.entity.dao.AbstractEntityDao;
import com.tle.core.entity.dao.EntityLockingDao;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.hibernate.type.HibernateEscapedString;

@SuppressWarnings("nls")
public abstract class AbstractEntityDaoImpl<T extends BaseEntity> extends GenericInstitionalDaoImpl<T, Long>
	implements
		AbstractEntityDao<T>
{
	@Inject
	private EntityLockingDao entityLockingDao;

	public AbstractEntityDaoImpl(Class<T> persistentClass)
	{
		super(persistentClass);
	}

	@Override
	public List<BaseEntityLabel> listAll(String resolveVirtualTo)
	{
		return listAll(resolveVirtualTo, null, false);
	}

	protected List<BaseEntityLabel> listAll(String resolveVirtualTo, final ListCallback callback)
	{
		return listAll(resolveVirtualTo, callback, false);
	}

	protected List<BaseEntityLabel> listAll(String resolveVirtualTo, final ListCallback callback,
		final boolean includeSystem)
	{
		@SuppressWarnings("unchecked")
		List<BaseEntityLabel> results = getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				// NOTE: Don't order by name here - use the sorting on
				// DynamicHtmlListModel
				StringBuilder hql = new StringBuilder();
				hql.append("SELECT ");
				if( callback != null && callback.isDistinct() )
				{
					hql.append("DISTINCT ");
				}
				hql.append("NEW com.tle.beans.entity.BaseEntityLabel");
				hql.append("(be.id, be.uuid, be.name.id, be.owner, be.systemType) FROM ");
				hql.append(getPersistentClass().getName());
				hql.append(" be ");
				if( callback != null && !Check.isEmpty(callback.getAdditionalJoins()) )
				{
					hql.append(" ");
					hql.append(callback.getAdditionalJoins());
					hql.append(" ");
				}
				hql.append("WHERE be.institution = :institution");

				if( !includeSystem )
				{
					hql.append(" AND be.systemType = false");
				}

				if( callback != null && !Check.isEmpty(callback.getAdditionalWhere()) )
				{
					hql.append(" AND ");
					hql.append(callback.getAdditionalWhere());
				}

				Query query = session.createQuery(hql.toString());
				query.setParameter("institution", CurrentInstitution.get());
				query.setCacheable(true);

				if( callback != null )
				{
					callback.processQuery(query);
				}

				return query.list();
			}
		});

		if( resolveVirtualTo != null )
		{
			String privType = resolveVirtualTo;
			for( BaseEntityLabel result : results )
			{
				result.setPrivType(privType);
			}
		}

		return results;
	}

	@Override
	public List<BaseEntityLabel> listEnabled(final String resolveVirtualTo)
	{
		return listAll(resolveVirtualTo, new EnabledCallback(null), false);
	}

	@Override
	public List<BaseEntityLabel> listAllIncludingSystem(final String resolveVirtualTo)
	{
		return listAll(resolveVirtualTo, null, true);
	}

	@Override
	public void delete(T entity)
	{
		entityLockingDao.deleteForEntity(entity);
		super.delete(entity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<T> getByIds(Collection<Long> ids)
	{
		if( ids == null || ids.isEmpty() )
		{
			return Collections.emptyList();
		}

		List<T> entityList = getHibernateTemplate()
			.findByNamedParam("from " + getPersistentClass().getName() + " where id in (:keys)", "keys", ids);
		return entityList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<T> getByUuids(Collection<String> ids)
	{
		if( ids == null || ids.isEmpty() )
		{
			return Collections.emptyList();
		}

		List<T> entityList = getHibernateTemplate().findByNamedParam(
			"from " + getPersistentClass().getName() + " where institution = :institution and uuid in (:keys)",
			new String[]{"institution", "keys"}, new Object[]{CurrentInstitution.get(), ids});
		return entityList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getByUuid(String uuid)
	{
		List<T> results = getHibernateTemplate().find(
			"FROM " + getPersistentClass().getName() + " WHERE institution = ? AND uuid = ?",
			new Object[]{CurrentInstitution.get(), uuid});
		return results.isEmpty() ? null : results.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getReferencedUsers()
	{
		List<String> entityList = getHibernateTemplate().findByNamedParam(
			"select distinct owner from " + getPersistentClass().getName() + " where institution = :institution",
			"institution", CurrentInstitution.get());
		return new HashSet<String>(entityList);
	}

	public void setEntityLockingDao(EntityLockingDao entityLockingDao)
	{
		this.entityLockingDao = entityLockingDao;
	}

	@Override
	@Transactional
	public List<T> enumerateAll()
	{
		return enumerateAll((ListCallback)null);
	}

	@Override
	@Transactional
	public List<T> enumerateAll(EnumerateOptions options)
	{
		ListCallback callback = null;
		if (options != null)
		{
			if (!Check.isEmpty(options.getQuery()) || options.getMax() != -1 || options.getOffset() != 0)
			{
				callback = getSearchListCallback(callback, options);
			}
			Boolean includeSystem = options.isIncludeSystem();
			if (includeSystem != null)
			{
				callback = new SystemCallback(callback, options.isIncludeSystem());
			}
		}
		return super.enumerateAll(callback);
	}

	@Override
	protected List<T> enumerateAll(ListCallback callback)
	{
		return super.enumerateAll(new SystemCallback(callback, false));
	}

	@Override
	@Transactional
	public List<T> enumerateEnabled()
	{
		return super.enumerateAll(new EnabledCallback(null));
	}

	@Override
	@Transactional
	public List<Long> enumerateAllIds()
	{
		return enumerateAllIds(false);
	}

	@Override
	@Transactional
	public List<Long> enumerateAllIdsIncludingSystem()
	{
		return enumerateAllIds(true);
	}

	@SuppressWarnings("unchecked")
	protected List<Long> enumerateAllIds(final boolean includeSystem)
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				// NOTE: Don't order by name here - use NumberStringComparator
				// on the returned list.
				StringBuilder hql = new StringBuilder("select id from ");
				hql.append(getPersistentClass().getName());
				hql.append(" where institution = :institution");
				if( !includeSystem )
				{
					hql.append(" and systemType = false");
				}

				Query query = session.createQuery(hql.toString());
				query.setParameter("institution", CurrentInstitution.get());
				query.setCacheable(true);
				query.setReadOnly(true);
				return query.list();
			}
		});
	}

	@Override
	public String getUuidForId(final long id)
	{
		return (String) getHibernateTemplate().execute(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				StringBuilder hql = new StringBuilder("SELECT uuid FROM ");
				hql.append(getPersistentClass().getName());
				hql.append(" WHERE id = :id");

				Query query = session.createQuery(hql.toString());
				query.setCacheable(true);
				query.setReadOnly(true);
				query.setParameter("id", id); //$NON-NLS-1$
				return query.list().get(0);
			}
		});
	}

	@Override
	public List<T> search(String freetext, boolean allowArchived, int offset, int perPage)
	{
		return enumerateAll(getSearchListCallback(null,
				new EnumerateOptions(freetext, offset, perPage, false, allowArchived)));
	}

	protected DefaultSearchListCallback getSearchListCallback(final ListCallback nestedCallback, final EnumerateOptions options)
	{
		ListCallback callback = null;
		final Boolean includeDisabled = options.isIncludeDisabled();
		if (includeDisabled != null)
		{
			callback = new EnabledCallback(callback, includeDisabled);
		}
		if (options.getOffset() != 0 || options.getMax() != -1)
		{
			callback = new PagedListCallback(callback, options.getOffset(), options.getMax());
		}
		return new DefaultSearchListCallback(callback, options.getQuery());
	}

	protected static class EnabledCallback extends BaseCallback
	{
		/**
		 * Tri-state value: true = enabled only, false = disabled only, null =
		 * no filter
		 */
		private final Boolean enabled;

		public EnabledCallback(ListCallback wrappedCallback)
		{
			this(wrappedCallback, true);
		}

		/**
		 * @param enabled Tri-state value: true = enabled only, false = disabled
		 *            only, null = no filter
		 */
		public EnabledCallback(ListCallback wrappedCallback, Boolean enabled)
		{
			super(wrappedCallback);
			this.enabled = enabled;
		}

		@Override
		public String createAdditionalWhere()
		{
			if( enabled != null )
			{
				return "be.disabled = :disabled";
			}
			return null;
		}

		@Override
		public void processQuery(Query query)
		{
			super.processQuery(query);
			if( enabled != null )
			{
				query.setParameter("disabled", !enabled);
			}
		}
	}

	protected static class SystemCallback extends BaseCallback implements ListCallback
	{
		private final boolean includeSystem;

		public SystemCallback(ListCallback wrappedCallback, boolean includeSystem)
		{
			super(wrappedCallback);
			this.includeSystem = includeSystem;
		}

		@Override
		public String createAdditionalWhere()
		{
			if( !includeSystem )
			{
				return "be.systemType = false";
			}
			return null;
		}
	}

	protected static class PagedListCallback extends BaseCallback implements ListCallback
	{
		protected final int offset;
		protected final int max;

		public PagedListCallback(ListCallback wrappedCallback, int offset, int max)
		{
			super(wrappedCallback);
			this.offset = offset;
			this.max = max;
		}

		@Override
		public void processQuery(Query query)
		{
			super.processQuery(query);
			if (offset > 0)
			{
				query.setFirstResult(offset);
			}
			if (max >= 0)
			{
				query.setFetchSize(max);
				query.setMaxResults(max);
			}
		}
	}

	protected static class DefaultSearchListCallback extends BaseCallback implements ListCallback
	{
		protected final String freetext;

		@SuppressWarnings("null")
		public DefaultSearchListCallback(ListCallback wrappedCallback, String freetext)
		{
			super(wrappedCallback);
			String query = freetext;
			if( query != null )
			{
				// remove *'s from start and end if there is one
				while( query.endsWith("*") )
				{
					query = query.substring(0, query.length() - 1);
				}
				while( query.startsWith("*") )
				{
					query = query.substring(1);
				}
				query = query.replaceAll("\\*", "%");
			}
			this.freetext = (Check.isEmpty(query) ? null : '%' + query.trim().toLowerCase() + '%');
		}

		@Override
		public String createAdditionalJoins()
		{
			if( freetext != null )
			{
				return "LEFT JOIN be.name.strings ns LEFT JOIN be.description.strings ds";
			}
			return null;
		}

		@Override
		public String createAdditionalWhere()
		{
			String where = null;
			if( freetext != null )
			{
				// CAST required for SQLServer
				where = "(LOWER(CAST(ns.text AS string)) LIKE :freetext "
						+ "OR LOWER(CAST(ds.text AS string)) LIKE :freetext)";
			}
			return where;
		}

		@Override
		public void processQuery(Query query)
		{
			super.processQuery(query);
			if( freetext != null )
			{
				query.setParameter("freetext", freetext);
			}
		}
	}

	@Override
	public void removeOrphanedOwners(final String owner)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query query = session.createQuery("UPDATE " + getPersistentClass().getName() + " SET owner = '"
					+ HibernateEscapedString.MARK_EMPTY + "' WHERE owner = :owner AND institution = :institution");
				query.setParameter("owner", owner);
				query.setParameter("institution", CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}

	@Override
	public void changeOwnerId(final String fromOwnerId, final String toOwnerId)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query query = session.createQuery("UPDATE " + getPersistentClass().getName()
					+ " SET owner = :toOwner WHERE owner = :fromOwner AND institution = :institution");
				query.setParameter("toOwner", toOwnerId);
				query.setParameter("fromOwner", fromOwnerId);
				query.setParameter("institution", CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}
}
