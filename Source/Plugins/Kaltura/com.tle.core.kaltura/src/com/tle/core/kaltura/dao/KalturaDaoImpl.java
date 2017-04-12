package com.tle.core.kaltura.dao;

import javax.inject.Singleton;

import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;

@Bind(KalturaDao.class)
@Singleton
public class KalturaDaoImpl extends AbstractEntityDaoImpl<KalturaServer> implements KalturaDao
{
	public KalturaDaoImpl()
	{
		super(KalturaServer.class);
	}
}
