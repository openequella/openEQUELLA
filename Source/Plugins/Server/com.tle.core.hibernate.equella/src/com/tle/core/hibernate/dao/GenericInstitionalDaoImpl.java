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

package com.tle.core.hibernate.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;

public class GenericInstitionalDaoImpl<T, ID extends Serializable> extends GenericDaoImpl<T, ID>
	implements
		GenericInstitutionalDao<T, ID>
{
	public GenericInstitionalDaoImpl(Class<T> persistentClass)
	{
		super(persistentClass);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public List<T> enumerateAll()
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				// NOTE: Don't order by name here - use NumberStringComparator
				// on the returned list.
				Query query = session.createQuery("from " + getPersistentClass().getName() //$NON-NLS-1$
					+ " where institution = :institution"); //$NON-NLS-1$
				query.setParameter("institution", CurrentInstitution.get()); //$NON-NLS-1$
				query.setCacheable(true);
				query.setReadOnly(true);
				return query.list();
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional
	public List<ID> enumerateAllIds()
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				// NOTE: Don't order by name here - use NumberStringComparator
				// on the returned list.
				Query query = session.createQuery("select id from " + getPersistentClass().getName() //$NON-NLS-1$
					+ " where institution = :institution"); //$NON-NLS-1$
				query.setParameter("institution", CurrentInstitution.get()); //$NON-NLS-1$
				query.setCacheable(true);
				query.setReadOnly(true);
				return query.list();
			}
		});
	}

	@SuppressWarnings({"unchecked", "nls"})
	protected List<T> enumerateAll(final ListCallback callback)
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				// NOTE: Don't order by name here - use NumberStringComparator
				// on the returned list.
				StringBuilder hql = new StringBuilder();
				hql.append("SELECT ");
				if( callback != null && callback.isDistinct() )
				{
					hql.append("DISTINCT ");
				}
				hql.append("be FROM ");
				hql.append(getPersistentClass().getName());
				hql.append(" be ");
				if( callback != null && !Check.isEmpty(callback.getAdditionalJoins()) )
				{
					hql.append(" ");
					hql.append(callback.getAdditionalJoins());
					hql.append(" ");
				}
				hql.append("WHERE be.institution = :institution");

				if( callback != null && !Check.isEmpty(callback.getAdditionalWhere()) )
				{
					hql.append(" AND ");
					hql.append(callback.getAdditionalWhere());
				}

				if( callback != null && callback.getOrderBy() != null )
				{
					hql.append(callback.getOrderBy());
				}

				Query query = session.createQuery(hql.toString());
				query.setParameter("institution", CurrentInstitution.get());
				query.setCacheable(true);
				query.setReadOnly(true);

				if( callback != null )
				{
					callback.processQuery(query);
				}

				List<T> res = query.list();
				return res;
			}
		});
	}

	public interface ListCallback
	{
		String getAdditionalJoins();

		String getAdditionalWhere();

		String getOrderBy();

		void processQuery(Query query);

		boolean isDistinct();
	}

	protected static class BaseCallback implements ListCallback
	{
		@Override
		public String getAdditionalJoins()
		{
			return null;
		}

		@Override
		public String getAdditionalWhere()
		{
			return null;
		}

		@Override
		public String getOrderBy()
		{
			return null;
		}

		@Override
		public void processQuery(Query query)
		{
			// Nada
		}

		@Override
		public boolean isDistinct()
		{
			return false;
		}

		@SuppressWarnings("nls")
		protected String appendWhere(String where, String extra)
		{
			if( Check.isEmpty(where) )
			{
				return extra;
			}
			return where + " AND " + extra;
		}
	}
}
