package com.tle.cla.web;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.cla.service.CLAService;
import com.tle.cla.web.viewitem.summary.CLAAgreementDialog;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.AbstractCopyrightResourceViewerFilter;
import com.tle.web.copyright.section.AbstractCopyrightAgreementDialog;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class CLAResourceViewerFilter extends AbstractCopyrightResourceViewerFilter
{
	@Inject
	private CLAService claService;

	@Override
	protected Class<? extends AbstractCopyrightAgreementDialog> getDialogClass()
	{
		return CLAAgreementDialog.class;
	}

	@Override
	protected CopyrightService<?, ?, ?> getCopyrightService()
	{
		return claService;
	}
}
