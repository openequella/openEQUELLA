package com.tle.core.payment.dao.impl;

import javax.inject.Singleton;

import com.tle.common.payment.entity.Region;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.RegionDao;

@Bind(RegionDao.class)
@Singleton
public class RegionDaoImpl extends AbstractEntityDaoImpl<Region> implements RegionDao
{
	public RegionDaoImpl()
	{
		super(Region.class);
	}
}
