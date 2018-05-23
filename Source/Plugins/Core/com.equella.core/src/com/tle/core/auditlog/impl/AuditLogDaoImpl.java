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

package com.tle.core.auditlog.impl;

import com.google.inject.Singleton;
import com.tle.beans.Institution;
import com.tle.beans.audit.AuditLogEntry;
import com.tle.core.auditlog.AuditLogDao;
import com.tle.core.guice.Bind;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

@Bind(AuditLogDao.class)
@Singleton
public class AuditLogDaoImpl extends AbstractAuditLogDaoImpl<AuditLogEntry> implements AuditLogDao
{
	public AuditLogDaoImpl()
	{
		super(AuditLogEntry.class);
	}

	@Override
	public Iterator<AuditLogEntry> listForUser(Institution inst, String userId)
	{
		return (Iterator<AuditLogEntry>) getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session.createQuery("from AuditLogEntry where institution = :inst and userId = :user");
				q.setParameter("inst", inst);
				q.setParameter("user", userId);
				return q.iterate();
			}
		});
	}

	@Override
	@Transactional
	public void deleteForUser(Institution inst, String userId)
	{
		getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session.createQuery("delete from AuditLogEntry where institution = :inst and userId = :user");
				q.setParameter("inst", inst);
				q.setParameter("user", userId);
				return q.executeUpdate();
			}
		});
	}


}
