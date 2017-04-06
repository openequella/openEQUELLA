package com.tle.core.payment.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.payment.dao.SubscriptionPeriodDao;
import com.tle.core.user.CurrentInstitution;

@Bind(SubscriptionPeriodDao.class)
@Singleton
public class SubscriptionPeriodDaoImpl extends GenericInstitionalDaoImpl<SubscriptionPeriod, Long>
	implements
		SubscriptionPeriodDao
{
	public SubscriptionPeriodDaoImpl()
	{
		super(SubscriptionPeriod.class);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAll()
	{
		for( SubscriptionPeriod period : enumerateAll() )
		{
			delete(period);
		}
	}

	@Override
	@SuppressWarnings("nls")
	public SubscriptionPeriod getByUuid(String uuid)
	{
		return findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()), Restrictions.eq("uuid", uuid));
	}

	/**
	 * Assuming the required order is always ascending by multitude. We probably
	 * don't need any other.
	 */
	@SuppressWarnings({"unchecked", "nls"})
	@Override
	public List<SubscriptionPeriod> enumerateAll()
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public List<SubscriptionPeriod> doInHibernate(Session session) throws HibernateException
			{
				Query query = session.createQuery("from SubscriptionPeriod sp where sp.institution = :institution "
					+ " order by sp.magnitude asc ");
				query.setParameter("institution", CurrentInstitution.get());
				query.setCacheable(true);
				query.setReadOnly(true);
				return query.list();
			}
		});
	}
}
