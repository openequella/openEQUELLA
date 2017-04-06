package com.tle.cal.web.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.cal.service.CALService;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.service.impl.AbstractCopyrightWebService;

@Bind
@Singleton
public class CALWebServiceImpl extends AbstractCopyrightWebService<CALHolding>
{
	@Inject
	private CALService calService;

	@Override
	public CopyrightService<CALHolding, CALPortion, CALSection> getCopyrightServiceImpl()
	{
		return calService;
	}
}
