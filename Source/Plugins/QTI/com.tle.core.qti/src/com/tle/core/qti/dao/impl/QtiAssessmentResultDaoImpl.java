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

package com.tle.core.qti.dao.impl;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.qti.entity.QtiAssessmentResult;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.dao.helpers.ScrollableResultsIterator;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.qti.dao.QtiAssessmentResultDao;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(QtiAssessmentResultDao.class)
@Singleton
public class QtiAssessmentResultDaoImpl extends GenericDaoImpl<QtiAssessmentResult, Long>
	implements
		QtiAssessmentResultDao
{
	public QtiAssessmentResultDaoImpl()
	{
		super(QtiAssessmentResult.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<QtiAssessmentResult> findByAssessmentTest(QtiAssessmentTest test)
	{
		return getHibernateTemplate().find("from QtiAssessmentResult where test = ?", test);
	}

	private Criterion[] getBaseCriteria(QtiAssessmentTest test, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid)
	{
		return new Criterion[]{Restrictions.eq("test", test), Restrictions.eq("resourceLinkId", resourceLinkId),
				Restrictions.eq("userId", userId), Restrictions.eq("lmsInstanceId", toolConsumerInstanceGuid)};
	}

	/**
	 * Only counts _complete_ attempts
	 * 
	 * @param test
	 * @param resourceLinkId
	 * @param userId
	 * @param toolConsumerInstanceGuid
	 * @return
	 */
	@Override
	public int countAttemptsByResourceLink(QtiAssessmentTest test, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid)
	{
		return (int) countByCriteria(getBaseCriteria(test, resourceLinkId, userId, toolConsumerInstanceGuid));
	}

	/**
	 * In the case of multiple attempts you will receive a list of results. Any
	 * non-submitted sessionStatus result should be continued.
	 * 
	 * @param test
	 * @param resourceLinkId
	 * @param userId
	 * @param toolConsumerInstanceGuid
	 * @return
	 */
	@Override
	public List<QtiAssessmentResult> findByResourceLink(QtiAssessmentTest test, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid)
	{
		return findAllByCriteria(Order.desc("datestamp"), -1,
			getBaseCriteria(test, resourceLinkId, userId, toolConsumerInstanceGuid));
	}

	@Override
	public QtiAssessmentResult getCurrentByResourceLink(QtiAssessmentTest test, String resourceLinkId, String userId,
		String toolConsumerInstanceGuid)
	{
		List<QtiAssessmentResult> findByResourceLink = findByResourceLink(test, resourceLinkId, userId,
			toolConsumerInstanceGuid);
		if( findByResourceLink.size() > 0 )
		{
			return findByResourceLink.get(0);
		}
		return null;
	}

	private Query getAllQuery(Session session)
	{
		final Query query = session
			.createQuery("FROM QtiAssessmentResult result WHERE result.test.institution = :institution");
		query.setParameter("institution", CurrentInstitution.get());
		return query;
	}

	@SuppressWarnings("unchecked")
	public List<QtiAssessmentResult> listAll()
	{
		return (List<QtiAssessmentResult>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				final Query query = getAllQuery(session);
				return query.list();
			}
		});
	}

	@Override
	public Iterator<QtiAssessmentResult> getIterator()
	{
		final ScrollableResults cinnamonScroll = (ScrollableResults) getHibernateTemplate()
			.execute(new HibernateCallback()
			{
				@Override
				public Object doInHibernate(Session session) throws HibernateException, SQLException
				{
					final Query query = getAllQuery(session);
					query.setReadOnly(true);
					return query.scroll(ScrollMode.FORWARD_ONLY);
				}
			});
		return new ScrollableResultsIterator<QtiAssessmentResult>(cinnamonScroll);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public void deleteAll()
	{
		for( QtiAssessmentResult result : listAll() )
		{
			delete(result);
			flush();
			clear();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<QtiAssessmentResult> findAllByCriteria(@Nullable final Order order, final int firstResult,
		final int maxResults, final Criterion... criterion)
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
				criteria.createAlias("itemResults", "ir", CriteriaSpecification.LEFT_JOIN);
				return criteria.list();
			}
		});
	}
}
