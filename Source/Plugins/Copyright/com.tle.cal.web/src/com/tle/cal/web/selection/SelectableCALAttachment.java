package com.tle.cal.web.selection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.cal.service.CALService;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.AbstractSelectableCopyrightAttachment;

@Bind
@Singleton
public class SelectableCALAttachment extends AbstractSelectableCopyrightAttachment<CALHolding, CALPortion, CALSection>
{
	@Inject
	private CALService calService;

	@Override
	protected CopyrightService getCopyrightServiceImpl()
	{
		return calService;
	}

}
