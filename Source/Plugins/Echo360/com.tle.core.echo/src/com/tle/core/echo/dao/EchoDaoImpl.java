package com.tle.core.echo.dao;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.tle.beans.Institution;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.echo.entity.EchoServer;
import com.tle.core.guice.Bind;

@Bind(EchoDao.class)
@Singleton
@SuppressWarnings("nls")
public class EchoDaoImpl extends AbstractEntityDaoImpl<EchoServer> implements EchoDao
{
	public EchoDaoImpl()
	{
		super(EchoServer.class);
	}

	@Override
	public EchoServer getBySystemID(Institution inst, String esid)
	{
		return findByCriteria(Restrictions.eq("echoSystemID", esid), Restrictions.eq("institution", inst));
	}
}