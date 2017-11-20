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

package com.tle.core.dao.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.beans.UserPreference;
import com.tle.core.dao.UserPreferenceDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.common.institution.CurrentInstitution;

@SuppressWarnings("nls")
@Bind(UserPreferenceDao.class)
@Singleton
public class UserPreferenceDaoImpl extends GenericDaoImpl<UserPreference, UserPreference.UserPrefKey>
	implements
		UserPreferenceDao
{
	public UserPreferenceDaoImpl()
	{
		super(UserPreference.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserPreference> enumerateAll()
	{
		return getHibernateTemplate().find("from UserPreference where key.institution = ?",
			CurrentInstitution.get().getDatabaseId());
	}

	@Override
	public void deleteAll()
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("delete from UserPreference where key.institution = :i");
				query.setParameter("i", CurrentInstitution.get().getDatabaseId());
				query.executeUpdate();
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getReferencedUsers()
	{
		final List<String> userIds = getHibernateTemplate().find(
			"select distinct u.key.userID from UserPreference u where u.key.institution = ?",
			CurrentInstitution.get().getDatabaseId());
		final Set<String> userIdSet = new HashSet<String>(userIds.size());
		userIdSet.addAll(userIds);
		return userIdSet;
	}

	@Override
	public void transferUserId(final String fromUserId, final String toUserId)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query query = session.createQuery("UPDATE UserPreference"
					+ " SET key.userID = :toUserId WHERE key.userID = :fromUserId AND key.institution = :i");
				query.setParameter("toUserId", toUserId);
				query.setParameter("fromUserId", fromUserId);
				query.setParameter("i", CurrentInstitution.get().getDatabaseId());
				query.executeUpdate();
				return null;
			}
		});
	}
}
