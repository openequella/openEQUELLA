package com.tle.cla.web.viewitem.summary;

import javax.inject.Inject;

import com.tle.cla.CLAConstants;
import com.tle.cla.web.service.CLAWebServiceImpl;
import com.tle.core.copyright.Holding;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.section.AbstractActivateSection;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.sections.TreeIndexed;

@TreeIndexed
@Bind
public class CLAActivateSection extends AbstractActivateSection
{
	@Inject
	private CLAWebServiceImpl claWebService;

	@Override
	protected String getActivationType()
	{
		return CLAConstants.ACTIVATION_TYPE;
	}

	@Override
	protected CopyrightWebService<? extends Holding> getCopyrightServiceImpl()
	{
		return claWebService;
	}
}
