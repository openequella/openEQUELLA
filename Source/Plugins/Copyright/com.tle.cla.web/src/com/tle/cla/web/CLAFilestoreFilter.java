package com.tle.cla.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.cla.service.CLAService;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.AbstractCopyrightFilestoreFilter;

@Bind
@Singleton
public class CLAFilestoreFilter extends AbstractCopyrightFilestoreFilter<CLAHolding, CLAPortion, CLASection>
{
	@Inject
	private CLAService claService;

	@Override
	protected CopyrightService<CLAHolding, CLAPortion, CLASection> getCopyrightService()
	{
		return claService;
	}

}
