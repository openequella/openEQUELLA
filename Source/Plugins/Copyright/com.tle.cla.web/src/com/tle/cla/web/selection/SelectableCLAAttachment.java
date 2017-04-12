package com.tle.cla.web.selection;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.tle.beans.cla.CLAHolding;
import com.tle.beans.cla.CLAPortion;
import com.tle.beans.cla.CLASection;
import com.tle.cla.service.CLAService;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.AbstractSelectableCopyrightAttachment;

@Bind
@Singleton
public class SelectableCLAAttachment extends AbstractSelectableCopyrightAttachment<CLAHolding, CLAPortion, CLASection>
{

	@Inject
	private CLAService claService;

	@Override
	protected CopyrightService<CLAHolding, CLAPortion, CLASection> getCopyrightServiceImpl()
	{
		return claService;
	}

}
