package com.tle.cal.web;

import javax.inject.Inject;

import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.cal.web.service.CALWebServiceImpl;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.AbstractCopyrightAttachmentFilter;
import com.tle.web.copyright.service.CopyrightWebService;

@Bind
public class CALAttachmentViewFilter extends AbstractCopyrightAttachmentFilter<CALHolding, CALPortion, CALSection>
{

	@Inject
	private CALWebServiceImpl calWebService;

	@Override
	protected CopyrightService<CALHolding, CALPortion, CALSection> getCopyrightServiceImpl()
	{
		return calWebService.getCopyrightServiceImpl();
	}

	@Override
	protected CopyrightWebService<CALHolding> getCopyrightWebServiceImpl()
	{
		return calWebService;
	}

}
