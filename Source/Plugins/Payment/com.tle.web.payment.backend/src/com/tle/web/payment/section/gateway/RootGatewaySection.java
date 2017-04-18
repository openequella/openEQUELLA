package com.tle.web.payment.section.gateway;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
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
@Bind
public class RootGatewaySection extends AbstractRootEntitySection<OneColumnLayoutModel>
{
	@PlugKey("gateway.page.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.error.license")
	private static Label LABEL_ERROR_LICENSE;

	@Inject
	private TLEAclManager aclService;

	@Override
	protected boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PaymentConstants.PRIV_CREATE_PAYMENT_GATEWAY,
			PaymentConstants.PRIV_EDIT_PAYMENT_GATEWAY).isEmpty();
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return GatewaySettingsSection.getShowGatewaysLink(info);
	}
}
