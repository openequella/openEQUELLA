package com.tle.web.payment.section.tax;

import javax.inject.Inject;

import com.tle.core.payment.PaymentConstants;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractRootEntitySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * @author Aaron
 */
public class RootTaxSection extends AbstractRootEntitySection<OneColumnLayoutModel>
{
	@PlugKey("tax.setting.title")
	private static Label LABEL_TITLE;
	// @PlugKey("error.noaccess")
	// private static Label LABEL_NO_ACCESS;
	@PlugKey("settings.error.license")
	private static Label LABEL_ERROR_LICENSE;

	@Inject
	private TLEAclManager aclService;

	@Override
	protected boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PaymentConstants.PRIV_CREATE_TAX, PaymentConstants.PRIV_EDIT_TAX)
			.isEmpty();
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return TaxSettingsLinkSection.getShowTaxesLink(info);
	}
}
