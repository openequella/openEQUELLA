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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.tle.beans.user.TLEGroup;
import com.tle.common.Check;
import com.tle.core.dao.impl.AbstractTreeDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.usermanagement.standard.dao.TLEGroupDao;
import com.tle.common.institution.CurrentInstitution;

/**
 * @author Nicholas Read
 */
@Bind(TLEGroupDao.class)
@Singleton
@SuppressWarnings("nls")
public class TLEGroupDaoImpl extends AbstractTreeDaoImpl<TLEGroup> implements TLEGroupDao
{
	public TLEGroupDaoImpl()
	{
		super(TLEGroup.class);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.user.TLEGroupDao#getGroupsContainingUser(java.lang.String
	 * )
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<TLEGroup> getGroupsContainingUser(String userID)
	{
		Preconditions.checkNotNull(userID);

		return getHibernateTemplate().findByNamedParam(
			"from TLEGroup g join g.users u where u = :userID and g.institution = :institution",
			new String[]{"userID", "institution"}, new Object[]{userID, CurrentInstitution.get()});
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.user.TLEGroupDao#listAllGroups()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<TLEGroup> listAllGroups()
	{
		return getHibernateTemplate().find("from TLEGroup where institution = ?", CurrentInstitution.get());
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getUsersInGroup(String parentGroupID, boolean recurse)
	{
		final TLEGroup parentGroup = findByUuid(parentGroupID);
		StringBuilder query = new StringBuilder("SELECT ELEMENTS(g.users) FROM TLEGroup g ");
		if( recurse )
		{
			query.append("LEFT OUTER JOIN g.allParents p ");
		}
		query.append("WHERE g.institution = :institution AND (g = :parentGroup");
		if( recurse )
		{
			query.append(" OR p = :parentGroup");
		}
		query.append(')');

		return getHibernateTemplate().findByNamedParam(query.toString(), new String[]{"parentGroup", "institution",},
			new Object[]{parentGroup, CurrentInstitution.get(),});
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public boolean addUserToGroup(final String groupUuid, final String userUuid)
	{
		return (boolean) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query query = session
					.createSQLQuery("SELECT id FROM tlegroup WHERE uuid = :groupUuid AND institution_id = :institutionId");
				query.setParameter("groupUuid", groupUuid);
				query.setParameter("institutionId", CurrentInstitution.get().getDatabaseId());
				final Number groupId = (Number) query.uniqueResult();

				query = session
					.createSQLQuery("SELECT COUNT(*) FROM tlegroup_users WHERE tlegroup_id = :groupId AND element = :userId");
				query.setParameter("groupId", groupId);
				query.setParameter("userId", userUuid);
				final Number count = (Number) query.uniqueResult();
				if( count.longValue() == 0 )
				{
					query = session
						.createSQLQuery("INSERT INTO tlegroup_users (tlegroup_id, element) VALUES (:groupId, :userId)");
					query.setParameter("groupId", groupId);
					query.setParameter("userId", userUuid);
					query.executeUpdate();
					session.clear();
					return true;
				}
				return false;
			}
		});
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public boolean removeUserFromGroup(final String groupUuid, final String userUuid)
	{
		return (boolean) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query query = session
					.createSQLQuery("SELECT id FROM tlegroup WHERE uuid = :groupUuid AND institution_id = :institutionId");
				query.setParameter("groupUuid", groupUuid);
				query.setParameter("institutionId", CurrentInstitution.get().getDatabaseId());
				final Number groupId = (Number) query.uniqueResult();

				query = session
					.createSQLQuery("DELETE FROM tlegroup_users WHERE tlegroup_id = :groupId AND element = :userId");
				query.setParameter("groupId", groupId);
				query.setParameter("userId", userUuid);
				final int rows = query.executeUpdate();
				session.clear();
				return rows > 0;
			}
		});
	}

	@Override
	public TLEGroup findByUuid(final String uuid)
	{
		return (TLEGroup) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("from TLEGroup g where g.uuid = :uuid AND g.institution = :i");
				query.setParameter("uuid", uuid);
				query.setParameter("i", CurrentInstitution.get());
				return query.uniqueResult();
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TLEGroup> getInformationForGroups(Collection<String> groups)
	{
		if( Check.isEmpty(groups) )
		{
			return new ArrayList<TLEGroup>();
		}
		return getHibernateTemplate().findByNamedParam(
			"from TLEGroup g where g.uuid in (:ids) and g.institution = :institution",
			new String[]{"ids", "institution"}, new Object[]{groups, CurrentInstitution.get()});
	}

	@Override
	public List<TLEGroup> searchGroups(String query, String parentId)
	{
		query = query.replace('*', '%');
		final TLEGroup parentGroup = findByUuid(parentId);
		if( parentGroup != null )
		{
			StringBuilder q = new StringBuilder("FROM TLEGroup g ");
			q.append("WHERE g.institution = :institution AND g.name LIKE :namequery AND :parent IN ELEMENTS(g.allParents)");

			return getHibernateTemplate().findByNamedParam(q.toString(),
				new String[]{"institution", "namequery", "parent",},
				new Object[]{CurrentInstitution.get(), query, parentGroup,});
		}
		return Lists.newArrayList();
	}
}
