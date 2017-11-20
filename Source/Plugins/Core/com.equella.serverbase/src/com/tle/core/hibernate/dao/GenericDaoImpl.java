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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * @author Nicholas Read
 */
@NonNullByDefault
public class GenericDaoImpl<T, ID extends Serializable> extends AbstractHibernateDao implements GenericDao<T, ID>
{
	protected static final String INSTITUTION = "institution"; //$NON-NLS-1$

	private final Class<T> persistentClass;

	public GenericDaoImpl(Class<T> persistentClass)
	{
		this.persistentClass = persistentClass;
	}

	@Override
	public Class<T> getPersistentClass()
	{
		return persistentClass;
	}

	public String getEntityName()
	{
		return persistentClass.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.GenericDao#save(null)
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.MANDATORY)
	public ID save(T entity)
	{
		ID result = (ID) getHibernateTemplate().save(entity);
		postSave(entity);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.GenericDao#saveOrUpdate(null)
	 */
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveOrUpdate(T entity)
	{
		getHibernateTemplate().saveOrUpdate(entity);
		postSave(entity);
	}

	protected void postSave(T entity)
	{
		// Nothing by default
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.GenericDao#update(null)
	 */
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void update(T entity)
	{
		getHibernateTemplate().update(entity);
		postSave(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A> A findAnyById(Class<A> clazz, Serializable id)
	{
		return (A) getHibernateTemplate().get(clazz, id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A> List<A> findAnyByCriteria(final DetachedCriteria criteria, @Nullable final Integer firstResult,
		@Nullable final Integer maxResults)
	{
		return (List<A>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Criteria execCriteria = criteria.getExecutableCriteria(session);
				if( firstResult != null )
				{
					execCriteria.setFirstResult(firstResult);
				}
				if( maxResults != null )
				{
					execCriteria.setMaxResults(maxResults);
				}
				return execCriteria.list();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.GenericDao#makeTransient(null)
	 */
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void delete(T entity)
	{
		getHibernateTemplate().delete(entity);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void unlinkFromSession(Object obj)
	{
		getHibernateTemplate().evict(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.GenericDao#findById(null, boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T findById(ID id)
	{
		return (T) getHibernateTemplate().get(getPersistentClass(), id);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.AbstractEntityDao#findByCriteria(org.hibernate.criterion
	 * .Criterion[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T findByCriteria(final Criterion... criterion)
	{
		return (T) getHibernateTemplate().execute(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Criteria criteria = createCriteria(session, criterion);
				return criteria.uniqueResult();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.GenericDao#countByCriteria(org.hibernate.criterion.Criterion
	 * [])
	 */
	@Override
	@Transactional
	public long countByCriteria(final Criterion... criterion)
	{
		return ((Number) getHibernateTemplate().execute(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Criteria criteria = createCriteria(session, criterion);
				criteria.setProjection(Projections.rowCount());
				return criteria.uniqueResult();
			}
		})).longValue();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.AbstractEntityDao#findAllByCriteria(org.hibernate.criterion
	 * .Criterion[])
	 */
	@Override
	public List<T> findAllByCriteria(final Criterion... criterion)
	{
		return findAllByCriteria(null, -1, criterion);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.GenericDao#findAllByCriteria(org.hibernate.criterion
	 * .Order, org.hibernate.criterion.Criterion[])
	 */
	@Override
	public List<T> findAllByCriteria(@Nullable final Order order, final int maxResults, final Criterion... criterion)
	{
		return findAllByCriteria(order, -1, maxResults, criterion);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.GenericDao#findAllByCriteria(org.hibernate.criterion
	 * .Order, int, int, org.hibernate.criterion.Criterion[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<T> findAllByCriteria(@Nullable final Order order, final int firstResult, final int maxResults,
		final Criterion... criterion)
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Criteria criteria = createCriteria(session, criterion);

				if( order != null )
				{
					criteria.addOrder(order);
				}
				if( firstResult > 0 )
				{
					criteria.setFirstResult(firstResult);
				}
				if( maxResults >= 0 )
				{
					criteria.setMaxResults(maxResults);
				}
				return criteria.list();
			}
		});
	}

	/**
	 * Extends HibernateCallback to add extra functionality.
	 * 
	 * @author Nicholas Read
	 */
	public abstract class TLEHibernateCallback implements HibernateCallback
	{
		public Criteria createCriteria(Session session, Criterion... criterion)
		{
			Criteria crit = session.createCriteria(getPersistentClass());
			if( criterion != null )
			{
				for( Criterion c : criterion )
				{
					if( c != null )
					{
						crit.add(c);
					}
				}
			}
			return crit;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.GenericDao#merge(null)
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.MANDATORY)
	public T merge(T entity)
	{
		return (T) getHibernateTemplate().merge(entity);
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.MANDATORY)
	public <O> O mergeAny(O obj)
	{
		return (O) getHibernateTemplate().merge(obj);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void saveAny(Object entity)
	{
		getHibernateTemplate().save(entity);
	}

	@Override
	public void flush()
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				session.flush();
				return null;
			}
		});
	}

	@Override
	public void clear()
	{
		getHibernateTemplate().clear();
	}

	@Override
	public void evict(T object)
	{
		getHibernateTemplate().evict(object);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAny(@Nullable Object object)
	{
		if( object != null )
		{
			getHibernateTemplate().delete(object);
		}
	}

	@SuppressWarnings("nls")
	protected T uniqueResult(List<T> results)
	{
		if( results.isEmpty() )
		{
			return null;
		}
		if( results.size() > 1 )
		{
			throw new RuntimeException("Expected unique result by found " + results.size() + " results");
		}
		return results.get(0);
	}
}
