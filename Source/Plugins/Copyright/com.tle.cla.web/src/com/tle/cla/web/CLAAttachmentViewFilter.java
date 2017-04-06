package com.tle.cla.web;

import javax.inject.Inject;

import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.cla.web.service.CLAWebServiceImpl;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.AbstractCopyrightAttachmentFilter;

@Bind
public class CLAAttachmentViewFilter extends AbstractCopyrightAttachmentFilter<CLAHolding, CLAPortion, CLASection>
{

	@Inject
	CLAWebServiceImpl claWebService;

	@Override
	protected CLAWebServiceImpl getCopyrightWebServiceImpl()
	{
		return claWebService;
	}

	@Override
	protected CopyrightService<CLAHolding, CLAPortion, CLASection> getCopyrightServiceImpl()
	{
		return claWebService.getCopyrightServiceImpl();
	}

}
