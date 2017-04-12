package com.tle.core.auditlog.impl;

import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.beans.Institution;
import com.tle.beans.audit.AuditLogTable;
import com.tle.core.auditlog.AuditLogExtensionDao;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class AbstractAuditLogDaoImpl<T extends AuditLogTable> extends GenericDaoImpl<T, Long>
	implements
		AuditLogExtensionDao<T>
{

	public AbstractAuditLogDaoImpl(Class<T> persistentClass)
	{
		super(persistentClass);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.dao.AuditLogDao#removeEntriesBeforeDate(java.util.Date)
	 */
	@Override
	public void removeEntriesBeforeDate(final Date date)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session.createQuery("delete from " + getEntityName()
					+ " a where a.timestamp < :date and a.institution = :institution");
				q.setTimestamp("date", date);
				q.setParameter("institution", CurrentInstitution.get());
				q.executeUpdate();

				return null;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.dao.AuditLogDao#removeEntriesForInstitution(com.tle.beans
	 * .Institution)
	 */
	@Override
	public void removeEntriesForInstitution(final Institution institution)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query q = session.createQuery("delete from " + getEntityName()
					+ " a where a.institution = :institution");
				q.setParameter("institution", institution);
				q.executeUpdate();

				return null;
			}
		});
	}
}
