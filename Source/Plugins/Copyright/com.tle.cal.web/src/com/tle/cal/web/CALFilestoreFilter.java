package com.tle.cal.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.cal.service.CALService;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.AbstractCopyrightFilestoreFilter;

@Bind
@Singleton
public class CALFilestoreFilter extends AbstractCopyrightFilestoreFilter<CALHolding, CALPortion, CALSection>
{
	@Inject
	private CALService calService;

	@Override
	protected CopyrightService<CALHolding, CALPortion, CALSection> getCopyrightService()
	{
		return calService;
	}

}
