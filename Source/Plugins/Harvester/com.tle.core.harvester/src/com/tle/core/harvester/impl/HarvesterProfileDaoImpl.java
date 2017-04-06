package com.tle.core.harvester.impl;

import java.util.Date;

import javax.inject.Singleton;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.tle.common.harvester.HarvesterProfile;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.HarvesterProfileDao;

@Bind(HarvesterProfileDao.class)
@Singleton
public class HarvesterProfileDaoImpl extends AbstractEntityDaoImpl<HarvesterProfile> implements HarvesterProfileDao
{

	public HarvesterProfileDaoImpl()
	{
		super(HarvesterProfile.class);
	}

	@SuppressWarnings("nls")
	@Override
	public void updateLastRun(final HarvesterProfile profile, final Date date)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				SQLQuery query = session.createSQLQuery("update harvester_profile set last_run = :date where id = :id");
				query.setDate("date", date);
				query.setLong("id", profile.getId());
				query.executeUpdate();
				return null;
			}
		});
	}

}
