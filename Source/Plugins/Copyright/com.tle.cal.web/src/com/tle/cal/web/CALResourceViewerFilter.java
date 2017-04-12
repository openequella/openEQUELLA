package com.tle.cal.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.cal.service.CALService;
import com.tle.cal.web.viewitem.summary.CALAgreementDialog;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.AbstractCopyrightResourceViewerFilter;
import com.tle.web.copyright.section.AbstractCopyrightAgreementDialog;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class CALResourceViewerFilter extends AbstractCopyrightResourceViewerFilter
{

	@Inject
	private CALService calService;

	@Override
	protected Class<? extends AbstractCopyrightAgreementDialog> getDialogClass()
	{
		return CALAgreementDialog.class;
	}

	@Override
	protected CopyrightService<?, ?, ?> getCopyrightService()
	{
		return calService;
	}
}
