package com.tle.cal.web.itemlist;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.cal.service.CALService;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.itemlist.AbstractCitationGenerator;
import com.tle.web.sections.SectionTree;

@Bind
@Singleton
public class CALListExtension extends AbstractCitationGenerator<CALHolding, CALPortion, CALSection>
{
	@Inject
	private CALService calService;

	@Override
	public void register(SectionTree tree, String parentId)
	{
		// Nothing to do
	}

	@Override
	public CopyrightService<CALHolding, CALPortion, CALSection> getCopyrightService()
	{
		return calService;
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}
