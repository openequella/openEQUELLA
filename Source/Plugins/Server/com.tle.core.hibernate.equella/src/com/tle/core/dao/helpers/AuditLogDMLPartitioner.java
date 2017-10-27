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

package com.tle.core.dao.helpers;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.beans.Institution;

@SuppressWarnings("nls")
public abstract class AuditLogDMLPartitioner implements HibernateCallback{

	private final Log log = LogFactory.getLog(AuditLogDMLPartitioner.class);
	
	private static final int MAX_BATCH_SIZE = 500;
	
	private final Date daysBeforeRemoval;
	private final Institution institution;

	public AuditLogDMLPartitioner(Date date, Institution institution)
	{
		this.daysBeforeRemoval = date;
		this.institution = institution;
	}
	
	@Override
	public final Object doInHibernate(Session session) throws HibernateException
	{
		return withSession(session);
	}

	private final int withSession(Session session) {
		log.debug("inside withSession() method");
		final String idFinderHql = buildIdFinderHql();
		final String dmlHql = buildDeleteAuditLogDmlHql();

		int total = 0;

		long startId = -1;
		long endId = -1;
		while( true )
		{
			// Find the next start and end ID
			startId = endId + 1;
			Query idFinder = session.createQuery(idFinderHql);
			idFinder.setMaxResults(MAX_BATCH_SIZE);
			idFinder.setParameter("startId", startId);		
			idFinder.setParameter("date", daysBeforeRemoval);
			idFinder.setParameter("institution", institution);
			ScrollableResults sr = idFinder.scroll();
			try
			{
				if( sr.last() )
				{				
					endId = sr.getLong(0);
				}
				else
				{
					// Nothing more to process
					log.debug("Total Audit Log Deleted Records :: "+total);
					return total;
				}
			}
			finally
			{
				sr.close();
			}
			
			Query dml = session.createQuery(dmlHql);
			dml.setParameter("startId", startId);
			dml.setParameter("endId", endId);
			dml.setParameter("date", daysBeforeRemoval);
			dml.setParameter("institution", institution);			
			total += dml.executeUpdate();
			log.debug("Start Id:: "+startId+" End Id:: "+endId+" Total Deleted Records ::"+total);
		}		
	}
	
	private String buildDeleteAuditLogDmlHql() {
		StringBuilder s = new StringBuilder();
		s.append("delete from ");
		s.append(getTableName());
		s.append(" WHERE timestamp < :date and institution = :institution");
		s.append(" AND (").append("id").append(" >= :startId");
		s.append(" AND ").append("id").append(" <= :endId)");
		log.debug("Delete Query:: "+s.toString());
		return s.toString();
	}

	private String buildIdFinderHql() {
		StringBuilder s = new StringBuilder();
		s.append("SELECT id FROM ");
		s.append(getTableName());
		s.append(" WHERE timestamp < :date and institution = :institution and id >= :startId ");
		s.append("ORDER BY id ASC");
		log.debug("Select Query:: "+s.toString());
		return s.toString();
	}
	
	public abstract String getTableName();
}
