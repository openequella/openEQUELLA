package com.tle.cla.dao;

import javax.inject.Singleton;

import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.core.copyright.dao.AbstractCopyrightDao;
import com.tle.core.guice.Bind;

@Bind(CLADao.class)
@Singleton
@SuppressWarnings("nls")
public class CLADaoImpl extends AbstractCopyrightDao<CLAHolding, CLAPortion, CLASection> implements CLADao
{

	@Override
	protected String getHoldingEntity()
	{
		return "CLAHolding";
	}

	@Override
	protected String getPortionEntity()
	{
		return "CLAPortion";
	}

	@Override
	protected String getSectionEntity()
	{
		return "CLASection";
	}

	@Override
	protected String getPortionTable()
	{
		return "cla_portion";
	}

	@Override
	protected String getHoldingTable()
	{
		return "cla_holding";
	}

}