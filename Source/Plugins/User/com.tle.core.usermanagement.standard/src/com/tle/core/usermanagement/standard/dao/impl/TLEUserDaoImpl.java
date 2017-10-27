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

package com.tle.core.usermanagement.standard.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.tle.beans.user.TLEUser;
import com.tle.common.Check;
import com.tle.core.dao.helpers.CollectionPartitioner;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.core.usermanagement.standard.dao.TLEUserDao;
import com.tle.core.usermanagement.standard.service.impl.TLEUserServiceImpl;
import com.tle.common.institution.CurrentInstitution;

/**
 * @author Nicholas Read
 */
@Bind(TLEUserDao.class)
@Singleton
@SuppressWarnings("nls")
public class TLEUserDaoImpl extends GenericDaoImpl<TLEUser, Long> implements TLEUserDao
{
	public TLEUserDaoImpl()
	{
		super(TLEUser.class);
	}

	@Override
	public long totalExistingUsers()
	{
		return (Long) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("select count(*) from TLEUser where institution = :institution");
				query.setParameter("institution", CurrentInstitution.get());
				return query.iterate().next();
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TLEUser> searchUsersInGroup(String userQuery, final String parentGroupID, boolean recurse)
	{
		// Prep the query by converting all *'s to %'s and lowercase it.
		userQuery = userQuery.replace('*', '%').toLowerCase();

		// Split it up on white space, removing leading/trailing % signs and
		// ignore empty strings.
		final Collection<String> tokens = Lists.newArrayList(Splitter.onPattern("\\s").trimResults(CharMatcher.is('%'))
			.omitEmptyStrings().split(userQuery));

		final StringBuilder q = new StringBuilder();
		q.append("FROM TLEUser t WHERE t.institution = :institution");

		for( int i = 0, size = tokens.size(); i < size; ++i )
		{
			q.append(" AND (LOWER(first_name) LIKE :token");
			q.append(i);
			q.append(" OR LOWER(last_name) LIKE :token");
			q.append(i);
			q.append(" OR LOWER(username) LIKE :token");
			q.append(i);
			q.append(')');
		}

		if( !Check.isEmpty(parentGroupID) )
		{
			q.append(" AND t.uuid IN (SELECT ELEMENTS(g.users) FROM TLEGroup g");
			if( !recurse )
			{
				q.append(" WHERE g.institution = :institution AND g.uuid = :groupID)");
			}
			else
			{
				q.append(" LEFT OUTER JOIN g.allParents sg WHERE g.institution = :institution AND (sg.uuid = :groupID OR g.uuid = :groupID))");
			}
		}

		return getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery(q.toString());
				query.setCacheable(true);
				query.setReadOnly(true);

				query.setParameter("institution", CurrentInstitution.get());

				int ti = 0;
				for( String t : tokens )
				{
					query.setParameter("token" + (ti++), '%' + t + '%');
				}

				if( !Check.isEmpty(parentGroupID) )
				{
					query.setParameter("groupID", parentGroupID);
				}

				return query.list();
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TLEUser> listAllUsers()
	{
		return getHibernateTemplate()
			.find("from TLEUser where institution = ?", new Object[]{CurrentInstitution.get()});
	}

	@Override
	@SecureOnCallSystem
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAll()
	{
		getHibernateTemplate().deleteAll(listAllUsers());
	}

	@Override
	public TLEUser findByUuid(final String uuid)
	{
		return (TLEUser) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("from TLEUser g where g.uuid = :uuid AND g.institution = :i");
				query.setParameter("uuid", uuid);
				query.setParameter("i", CurrentInstitution.get());
				return query.uniqueResult();
			}
		});
	}

	/**
	 * It seems to have been long possible for a username to be created that was
	 * spelt the same as a pre-existing one, differing only in case. This method
	 * however would prevent (among other things) either user from logging in
	 * because the LOWER(username) query would fail by not returning a unique
	 * result. From which we may conclude that there are no in-use duplicated
	 * usernames in the production world
	 * 
	 * @see TLEUserServiceImpl#usernameExists(String, String)
	 */
	@Override
	public TLEUser findByUsername(final String username)
	{
		return (TLEUser) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session
					.createQuery("FROM TLEUser WHERE LOWER(username) = :username AND institution = :i");
				query.setParameter("username", username.toLowerCase());
				query.setParameter("i", CurrentInstitution.get());
				return query.uniqueResult();
			}
		});
	}

	/**
	 * In determining that a candidate username may already be 'taken', we want
	 * a case-insensitive match, and exclude the possibility that an existing
	 * user will be compared to itself (by providing for unique uuid where
	 * available to be part of the query). We don't enforce a unique result here
	 * - it is enough that we can identify any pre-existing takers. Should any
	 * multiple of usernames exist, they would all be unworkable.
	 */
	@Override
	public boolean doesOtherUsernameSameSpellingExist(final String username, final String userUuid)
	{
		List<?> existingUsersList = (List<?>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				String queryString = "FROM TLEUser WHERE LOWER(username) = :username AND institution = :i ";
				if( !Check.isEmpty(userUuid) )
				{
					queryString += " AND NOT uuid = :uuid";
				}

				Query query = session.createQuery(queryString);
				query.setParameter("username", username.toLowerCase());
				query.setParameter("i", CurrentInstitution.get());
				if( !Check.isEmpty(userUuid) )
				{
					query.setParameter("uuid", userUuid);
				}
				return query.list();
			}
		});
		return !Check.isEmpty(existingUsersList);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TLEUser> getInformationForUsers(Collection<String> ids)
	{
		if( Check.isEmpty(ids) )
		{
			return new ArrayList<TLEUser>();
		}

		return getHibernateTemplate().executeFind(new CollectionPartitioner<String, TLEUser>(ids)
		{
			@Override
			public List<TLEUser> doQuery(Session session, Collection<String> collection)
			{
				return session.createQuery("FROM TLEUser u WHERE u.uuid in (:ids) AND u.institution = :i")
					.setParameterList("ids", collection).setParameter("i", CurrentInstitution.get()).list();
			}
		});
	}
}
