package com.tle.cal.dao;

import javax.inject.Singleton;

import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.core.copyright.dao.AbstractCopyrightDao;
import com.tle.core.guice.Bind;

@Bind(CALDao.class)
@Singleton
@SuppressWarnings("nls")
public class CALDaoImpl extends AbstractCopyrightDao<CALHolding, CALPortion, CALSection> implements CALDao
{
	@Override
	protected String getHoldingEntity()
	{
		return "CALHolding";
	}

	@Override
	protected String getPortionEntity()
	{
		return "CALPortion";
	}

	@Override
	protected String getSectionEntity()
	{
		return "CALSection";
	}

	@Override
	protected String getHoldingTable()
	{
		return "cal_holding";
	}

	@Override
	protected String getPortionTable()
	{
		return "cal_portion";
	}

}