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

package com.tle.core.portal.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.common.institution.CurrentInstitution;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.PortletPreference;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;

/**
 * @author aholland
 */
@Singleton
@SuppressWarnings("nls")
@Bind(PortletPreferenceDao.class)
public class PortletPreferenceDaoImpl extends GenericInstitionalDaoImpl<PortletPreference, Long>
	implements
		PortletPreferenceDao
{
	public PortletPreferenceDaoImpl()
	{
		super(PortletPreference.class);
	}

	@Override
	public PortletPreference getForPortlet(final String userId, final Portlet portlet)
	{
		return (PortletPreference) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session
					.createQuery("FROM PortletPreference WHERE userId = :userId" + " AND portlet = :portlet");
				query.setParameter("userId", userId);
				query.setParameter("portlet", portlet);
				return query.uniqueResult();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PortletPreference> getAllForPortlet(final Portlet portlet)
	{
		return (List<PortletPreference>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("FROM PortletPreference WHERE portlet = :portlet");
				query.setParameter("portlet", portlet);
				return query.list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PortletPreference> getForPortlets(final String userId, final Collection<Portlet> portlets)
	{
		if( portlets.size() == 0 )
		{
			return new ArrayList<PortletPreference>();
		}
		return (List<PortletPreference>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session
					.createQuery("FROM PortletPreference WHERE userId = :userId" + " AND portlet IN (:portlets)");
				query.setParameter("userId", userId);
				query.setParameterList("portlets", portlets);
				return query.list();
			}
		});
	}

	@Override
	public int deleteAllForPortlet(final Portlet portlet)
	{
		return (Integer) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("DELETE FROM PortletPreference WHERE portlet = :portlet");
				query.setParameter("portlet", portlet);
				return query.executeUpdate();
			}
		});
	}

	@Override
	public void deleteAllForUser(final String userId)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("DELETE FROM PortletPreference WHERE userId = :userId"
					+ " AND portlet IN (FROM Portlet WHERE institution = :institution)");
				query.setParameter("userId", userId);
				query.setParameter("institution", CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}

	@Override
	public void changeUserId(final String fromUserId, final String toUserId)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query query = session
					.createQuery("UPDATE PortletPreference SET userId = :toUserId WHERE userId = :fromUserId"
						+ " AND portlet IN (FROM Portlet WHERE institution = :institution)");
				query.setParameter("fromUserId", fromUserId);
				query.setParameter("toUserId", toUserId);
				query.setParameter("institution", CurrentInstitution.get());
				query.executeUpdate();
				return null;
			}
		});
	}
}
