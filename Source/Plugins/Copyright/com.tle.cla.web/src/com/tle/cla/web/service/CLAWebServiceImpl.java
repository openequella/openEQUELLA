package com.tle.cla.web.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.cla.service.CLAService;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.service.impl.AbstractCopyrightWebService;

@Bind
@Singleton
public class CLAWebServiceImpl extends AbstractCopyrightWebService<CLAHolding>
{

	@Inject
	private CLAService claService;

	@Override
	public CopyrightService<CLAHolding, CLAPortion, CLASection> getCopyrightServiceImpl()
	{
		return claService;
	}
}
