package com.tle.core.filesystem.staging.dao.impl;

import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Staging;
import com.tle.core.filesystem.staging.dao.StagingDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;

@NonNullByDefault
@Bind(StagingDao.class)
@Singleton
public class StagingDaoImpl extends GenericDaoImpl<Staging, String> implements StagingDao
{
	public StagingDaoImpl()
	{
		super(Staging.class);
	}

	@Override
	public void deleteAllForUserSession(String userSession)
	{
		Criterion[] cs = {Restrictions.eq("userSession", userSession)};
		for( Staging s : findAllByCriteria(cs) )
		{
			getHibernateTemplate().delete(s);
		}
	}
}
