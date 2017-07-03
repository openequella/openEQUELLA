package com.tle.core.item.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.beans.item.DrmAcceptance;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.item.dao.DrmAcceptanceDao;

/**
 * @author Nicholas Read
 */
@Singleton
@SuppressWarnings("nls")
@Bind(DrmAcceptanceDao.class)
public class DrmAcceptanceDaoImpl extends GenericDaoImpl<DrmAcceptance, Long> implements DrmAcceptanceDao
{
	public DrmAcceptanceDaoImpl()
	{
		super(DrmAcceptance.class);
	}

	@Override
	public void userIdChanged(final String fromUserId, final String toUserId)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				@SuppressWarnings("unchecked")
				List<DrmAcceptance> das = session
					.createQuery("FROM DrmAcceptance WHERE user = :userId AND item.institution = :institution")
					.setParameter("userId", fromUserId).setParameter("institution", CurrentInstitution.get()).list();
				for( DrmAcceptance da : das )
				{
					da.setUser(toUserId);
					session.save(da);
				}
				return null;
			}
		});
	}
}
