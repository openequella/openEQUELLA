package com.tle.cal.web.viewitem.summary;

import javax.inject.Inject;

import com.tle.cal.CALConstants;
import com.tle.cal.web.service.CALWebServiceImpl;
import com.tle.core.copyright.Holding;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.section.AbstractActivateSection;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;

@TreeIndexed
@Bind
public class CALActivateSection extends AbstractActivateSection
{
	@Inject
	private CALWebServiceImpl calWebService;
	@Inject
	private CALPercentageOverrideSection overrideSection;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerSections(overrideSection, id);
	}

	@Override
	protected CALPercentageOverrideSection getOverrideSection()
	{
		return overrideSection;
	}

	@Override
	protected String getActivationType()
	{
		return CALConstants.ACTIVATION_TYPE;
	}

	@Override
	protected CopyrightWebService<? extends Holding> getCopyrightServiceImpl()
	{
		return calWebService;
	}
}
