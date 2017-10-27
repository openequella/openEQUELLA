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

package com.tle.core.notification.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.notification.beans.Notification;

@Bind(NotificationDao.class)
@Singleton
@SuppressWarnings("nls")
public class NotificationDaoImpl extends GenericInstitionalDaoImpl<Notification, Long> implements NotificationDao
{
	// Sonar likes us to avoid repeated literal strings ...
	private static final String ATTEMPT = "attempt";
	private static final String REASONS = "reasons";
	private static final String NOTEID = "noteid";
	private static final String USER = "user";
	private static final String INST = "inst";
	private static final String ITEMID = "itemid";
	private static final String REASON = "reason";
	private static final String ITEMKEY = "itemkey";

	public NotificationDaoImpl()
	{
		super(Notification.class);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Notification getExistingNotification(ItemKey itemId, String reason, String user)
	{
		return getExistingNotification(itemId.toString(), reason, user);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public Notification getExistingNotification(String keyAsString, String reason, String user)
	{
		List<Notification> notifications = getHibernateTemplate().findByNamedParam(
			"from Notification where itemid = :itemid and reason = :reason and userTo = :user and institution = :inst",
			new String[]{ITEMID, REASON, USER, INST},
			new Object[]{keyAsString, reason, user, CurrentInstitution.get()});
		if( notifications.isEmpty() )
		{
			return null;
		}
		return notifications.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Notification> getNotificationsForItem(ItemId itemId, Institution institution)
	{
		List<Notification> notifications = getHibernateTemplate().findByNamedParam(
			"from Notification where itemidOnly = :itemid and institution = :inst", new String[]{ITEMID, INST},
			new Object[]{itemId.toString(), institution});
		return notifications;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAllForInstitution(Institution institution)
	{
		getHibernateTemplate().bulkUpdate("delete from Notification where institution = ?", institution);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAllForItem(ItemId itemId)
	{
		getHibernateTemplate().bulkUpdate("delete from Notification where institution = ? and itemidOnly = ?",
			new Object[]{CurrentInstitution.get(), itemId.toString()});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int deleteAllForItemReasons(final ItemId itemId, final Collection<String> reasons)
	{
		return ((Number) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				if( Check.isEmpty(reasons) )
				{
					return Integer.valueOf(0);
				}
				Query query = session.createQuery(
					"delete from Notification where itemidOnly = :itemid and reason in (:reasons) and institution = :inst");
				query.setParameter(ITEMID, itemId.toString());
				query.setParameterList(REASONS, reasons);
				query.setParameter(INST, CurrentInstitution.get());
				return query.executeUpdate();
			}
		})).intValue();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int deleteAllForUuidReasons(final String uuid, final Collection<String> reasons)
	{
		return ((Number) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				if( Check.isEmpty(reasons) )
				{
					return Integer.valueOf(0);
				}
				Query query = session.createQuery(
					"delete from Notification where itemid = :uuid and reason in (:reasons) and institution = :inst");
				query.setParameter("uuid", uuid);
				query.setParameterList(REASONS, reasons);
				query.setParameter(INST, CurrentInstitution.get());
				return query.executeUpdate();
			}
		})).intValue();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int deleteAllForKeyReasons(final ItemKey itemKey, final Collection<String> reasons)
	{
		return ((Number) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				if( Check.isEmpty(reasons) )
				{
					return Integer.valueOf(0);
				}
				Query query = session.createQuery(
					"delete from Notification where itemid = :itemkey and reason in (:reasons) and institution = :inst");
				query.setParameter(ITEMKEY, itemKey.toString());
				query.setParameterList(REASONS, reasons);
				query.setParameter(INST, CurrentInstitution.get());
				return query.executeUpdate();
			}
		})).intValue();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int deleteAllForUserKeyReasons(final ItemKey itemKey, final String userId, final Collection<String> reasons)
	{
		return ((Number) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				if( Check.isEmpty(reasons) )
				{
					return Integer.valueOf(0);
				}
				Query query = session.createQuery(
					"delete from Notification where itemid = :itemkey and userTo = :user and reason in (:reasons) and institution = :inst");
				query.setParameter(ITEMKEY, itemKey.toString());
				query.setParameter(USER, userId);
				query.setParameterList(REASONS, reasons);
				query.setParameter(INST, CurrentInstitution.get());
				return query.executeUpdate();
			}
		})).intValue();
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public boolean userIdChanged(final ItemKey itemKey, final String fromUserId, final String toUserId)
	{
		return (Boolean) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query query = session.createQuery("UPDATE Notification SET userTo = :toUserId"
					+ " WHERE userTo = :fromUserId AND itemid = :itemKey AND institution = :inst");
				query.setParameter("toUserId", toUserId);
				query.setParameter("fromUserId", fromUserId);
				query.setParameter(ITEMKEY, itemKey.toString());
				query.setParameter(INST, CurrentInstitution.get());
				return query.executeUpdate() > 0;
			}
		});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public NotifiedUser getUserToNotify(final Date notAfter, final String attemptId, final boolean batched)
	{
		return (NotifiedUser) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query query = session
					.createQuery("select n.userTo, i.uniqueId from Notification n join n.institution as i "
						+ "where n.processed = false and n.batched = :batched and (n.attemptId is null or n.attemptId <> :attempt) and "
						+ "(n.lastAttempt < :date or n.lastAttempt is null) group by i.uniqueId, n.userTo");
				query.setParameter("date", notAfter);
				query.setParameter("batched", batched);
				query.setParameter(ATTEMPT, attemptId);
				query.setMaxResults(1);
				Object[] vals = (Object[]) query.uniqueResult();
				if( vals == null )
				{
					return null;
				}
				return new NotifiedUser((String) vals[0], ((Number) vals[1]).longValue());
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<Notification> getNewestNotificationsForUser(final int maximum, final String user,
		final Collection<String> reasons, final String attemptId)
	{
		if( reasons.isEmpty() )
		{
			return Collections.emptyList();
		}
		return (List<Notification>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				if( Check.isEmpty(reasons) )
				{
					return new ArrayList<Notification>();
				}
				Query query = session
					.createQuery("from Notification where attemptId = :attempt and reason in (:reasons) "
						+ "and institution = :inst and userTo = :user and processed = false " + "order by date desc");
				query.setMaxResults(maximum);
				query.setParameter(ATTEMPT, attemptId);
				query.setParameterList(REASONS, reasons);
				query.setParameter(USER, user);
				query.setParameter(INST, CurrentInstitution.get());
				return query.list();
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.MANDATORY)
	public Map<String, Integer> getReasonCounts(final String user, final String attemptId)
	{
		return (Map<String, Integer>) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query query = session.createQuery("select count(reason), reason from Notification "
					+ "where attemptId = :attempt and processed = false "
					+ "and userTo = :user and institution = :inst group by reason");
				query.setParameter(ATTEMPT, attemptId);
				query.setParameter(USER, user);
				query.setParameter(INST, CurrentInstitution.get());
				Map<String, Integer> reasonMap = Maps.newHashMap();
				List<Object[]> counts = query.list();
				for( Object[] count : counts )
				{
					reasonMap.put((String) count[1], ((Number) count[0]).intValue());
				}
				return reasonMap;
			}
		});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int updateLastAttempt(String user, boolean batched, Date date, String attemptId)
	{
		return getHibernateTemplate().bulkUpdate(
			"update Notification set lastAttempt = ?, attemptId = ? "
				+ "where institution = ? and userTo = ? and processed = false and batched = ?",
			new Object[]{date, attemptId, CurrentInstitution.get(), user, batched});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int markProcessed(final String user, final Collection<String> reasons, final String attemptId)
	{
		if( reasons.isEmpty() )
		{
			return 0;
		}
		return (Integer) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("update Notification set processed = true "
					+ "where institution = :inst and userTo = :user and processed = false "
					+ "and reason in (:reasons) and attemptId = :attempt");
				query.setParameter(ATTEMPT, attemptId);
				query.setParameterList(REASONS, reasons);
				query.setParameter(USER, user);
				query.setParameter(INST, CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int deleteUnindexed(final String user, final Collection<String> reasons, final String attemptId)
	{
		if( reasons.isEmpty() )
		{
			return 0;
		}
		return (Integer) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery(
					"delete from Notification " + "where institution = :inst and userTo = :user and processed = false "
						+ "and reason in (:reasons) and attemptId = :attempt");

				query.setParameter(ATTEMPT, attemptId);
				query.setParameterList(REASONS, reasons);
				query.setParameter(USER, user);
				query.setParameter(INST, CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}


	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int markProcessedById(final String user, final Collection<Long> notifications, final String attemptId)
	{
		if( notifications.isEmpty() )
		{
			return 0;
		}
		return (Integer) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("update Notification set processed = true "
						+ "where institution = :inst and userTo = :user and processed = false "
						+ "and id in (:noteid) and attemptId = :attempt");
				query.setParameter(ATTEMPT, attemptId);
				query.setParameterList(NOTEID, notifications);
				query.setParameter(USER, user);
				query.setParameter(INST, CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int deleteUnindexedById(final String user, final Collection<Long> notifications, final String attemptId)
	{
		if( notifications.isEmpty() )
		{
			return 0;
		}
		return (Integer) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery(
						"delete from Notification " + "where institution = :inst and userTo = :user and processed = false "
								+ "and id in (:noteid) and attemptId = :attempt");

				query.setParameter(ATTEMPT, attemptId);
				query.setParameterList(NOTEID, notifications);
				query.setParameter(USER, user);
				query.setParameter(INST, CurrentInstitution.get());
				return query.executeUpdate();
			}
		});
	}
}
