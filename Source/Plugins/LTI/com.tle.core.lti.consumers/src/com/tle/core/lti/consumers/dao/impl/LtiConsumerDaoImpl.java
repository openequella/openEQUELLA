package com.tle.core.lti.consumers.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Session;

import com.tle.common.Check;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.dao.LtiConsumerDao;
import com.tle.core.user.CurrentInstitution;

@Bind(LtiConsumerDao.class)
@Singleton
public class LtiConsumerDaoImpl extends AbstractEntityDaoImpl<LtiConsumer> implements LtiConsumerDao
{
	public LtiConsumerDaoImpl()
	{
		super(LtiConsumer.class);
	}

	@Override
	public LtiConsumer findByConsumerKey(String consumerKey)
	{
		return (LtiConsumer) getHibernateTemplate().execute(new TLEHibernateCallback()
		{
			@SuppressWarnings("unchecked")
			@Override
			public Object doInHibernate(Session session)
			{
				List<LtiConsumer> consumers = session
					.createQuery("FROM LtiConsumer WHERE consumerKey = :key AND institution = :institution")
					.setParameter("key", consumerKey).setParameter("institution", CurrentInstitution.get()).list();
				return Check.isEmpty(consumers) ? null : consumers.get(0);
			}
		});
	}
}
