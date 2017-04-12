package com.tle.cla.web.viewitem.summary;

import com.tle.core.guice.Bind;
import com.tle.web.copyright.section.AbstractCopyrightAgreementDialog;
import com.tle.web.copyright.section.AbstractCopyrightAgreementSection;

/**
 * @author Aaron
 */
@Bind
public class CLAAgreementDialog extends AbstractCopyrightAgreementDialog
{
	@Override
	protected Class<? extends AbstractCopyrightAgreementSection> getAgreementSectionClass()
	{
		return CLAAgreementSection.class;
	}
}
