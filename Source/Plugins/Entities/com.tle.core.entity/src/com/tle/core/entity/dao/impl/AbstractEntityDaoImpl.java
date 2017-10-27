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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

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
		return listAll(resolveVirtualTo, new EnabledCallback(), false);
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
		return enumerateAll(null);
	}

	@Override
	protected List<T> enumerateAll(ListCallback callback)
	{
		return super.enumerateAll(new SystemCallback(callback, false));
	}

	@Override
	@Transactional
	public List<T> enumerateAllIncludingSystem()
	{
		return super.enumerateAll(new SystemCallback(null, true));
	}

	@Override
	@Transactional
	public List<T> enumerateEnabled()
	{
		return super.enumerateAll(new EnabledCallback());
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
		return enumerateAll(getSearchListCallback(freetext, allowArchived, offset, perPage));
	}

	protected ListCallback getSearchListCallback(final String freetext, boolean allowArchived, final int offset,
		final int perPage)
	{
		return new DefaultSearchListCallback(freetext, allowArchived, offset, perPage);
	}

	protected static class EnabledCallback extends BaseCallback
	{
		/**
		 * Tri-state value: true = enabled only, false = disabled only, null =
		 * no filter
		 */
		private final Boolean enabled;

		public EnabledCallback()
		{
			this(true);
		}

		/**
		 * @param enabled Tri-state value: true = enabled only, false = disabled
		 *            only, null = no filter
		 */
		public EnabledCallback(Boolean enabled)
		{
			this.enabled = enabled;
		}

		@Override
		public String getAdditionalWhere()
		{
			if( enabled != null )
			{
				return "be.disabled = :disabled";
			}
			return "";
		}

		@Override
		public void processQuery(Query query)
		{
			if( enabled != null )
			{
				query.setParameter("disabled", !enabled);
			}
		}
	}

	protected static class SystemCallback implements ListCallback
	{
		private final boolean includeSystem;
		private final ListCallback wrappedCallback;

		public SystemCallback(ListCallback wrappedCallback, boolean includeSystem)
		{
			this.wrappedCallback = wrappedCallback;
			this.includeSystem = includeSystem;
		}

		@Override
		public String getAdditionalJoins()
		{
			if( wrappedCallback != null )
			{
				return wrappedCallback.getAdditionalJoins();
			}
			return null;
		}

		@Override
		public String getAdditionalWhere()
		{
			String additional = null;
			if( !includeSystem )
			{
				additional = "be.systemType = false";
			}

			if( wrappedCallback != null && !Check.isEmpty(wrappedCallback.getAdditionalWhere()) )
			{
				String addWhere = wrappedCallback.getAdditionalWhere();
				if( additional != null )
				{
					additional += " AND " + addWhere;
				}
				else
				{
					additional = addWhere;
				}
			}

			return additional;
		}

		@Override
		public boolean isDistinct()
		{
			if( wrappedCallback != null )
			{
				return wrappedCallback.isDistinct();
			}

			// Do not change the original schematics - DISTINCT should be false
			// by default.
			return false;
		}

		@Override
		public void processQuery(Query query)
		{
			if( wrappedCallback != null )
			{
				wrappedCallback.processQuery(query);
			}
		}

		@Override
		public String getOrderBy()
		{
			if( wrappedCallback != null )
			{
				return wrappedCallback.getOrderBy();
			}
			return null;
		}
	}

	protected static class DefaultSearchListCallback implements ListCallback
	{
		protected final String freetext;
		protected final int offset;
		protected final int max;
		protected final boolean allowArchived;

		@SuppressWarnings("null")
		public DefaultSearchListCallback(String freetext, boolean allowArchived, int offset, int max)
		{
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
			this.allowArchived = allowArchived;
			this.offset = offset;
			this.max = max;
		}

		@Override
		public String getAdditionalJoins()
		{
			String joins = "";
			if( freetext != null )
			{
				joins = "LEFT JOIN be.name.strings ns LEFT JOIN be.description.strings ds";
			}
			return joins;
		}

		@Override
		public String getAdditionalWhere()
		{
			String where = null;
			if( freetext != null )
			{
				// CAST required for SQLServer
				where = addWhere(null,
					"(LOWER(CAST(ns.text AS string)) LIKE :freetext OR LOWER(CAST(ds.text AS string)) LIKE :freetext)");
			}
			if( !allowArchived )
			{
				where = addWhere(where, "be.disabled = false");
			}
			return where;
		}

		protected String addWhere(String where, String addition)
		{
			if( !Check.isEmpty(where) )
			{
				where += " AND " + addition; //$NON-NLS-1$
			}
			else
			{
				where = addition;
			}
			return where;
		}

		@Override
		public boolean isDistinct()
		{
			return false;
		}

		@Override
		public void processQuery(Query query)
		{
			if( freetext != null )
			{
				query.setParameter("freetext", freetext);
			}

			query.setFirstResult(offset);
			query.setFetchSize(max);
			query.setMaxResults(max);
		}

		@Override
		public String getOrderBy()
		{
			return null;
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
