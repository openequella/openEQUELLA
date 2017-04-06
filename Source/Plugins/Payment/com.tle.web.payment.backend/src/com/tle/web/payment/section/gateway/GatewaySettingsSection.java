package com.tle.web.payment.section.gateway;

import static com.tle.core.payment.PaymentConstants.PRIV_CREATE_PAYMENT_GATEWAY;
import static com.tle.core.payment.PaymentConstants.PRIV_EDIT_PAYMENT_GATEWAY;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.security.TLEAclManager;
import com.tle.core.system.LicenseService;
import com.tle.web.entities.section.AbstractEntitySettingsLinkSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@SuppressWarnings("nls")
@Bind
public class GatewaySettingsSection extends AbstractEntitySettingsLinkSection<Object>
{
	@PlugKey("gateway.setting.title")
	private static Label TITLE_LABEL;
	@PlugKey("gateway.setting.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private TLEAclManager aclService;
	@Inject
	private LicenseService licenseService;

	public static HtmlLinkState getShowGatewaysLink(SectionInfo info)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = info.createForward("/access/gateway.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PRIV_CREATE_PAYMENT_GATEWAY, PRIV_EDIT_PAYMENT_GATEWAY).isEmpty()
			&& licenseService.isFeatureEnabled(PaymentConstants.LICENSE_FEATURE_CONTENT_EXCHANGE);
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return getShowGatewaysLink(info);
	}

	@Override
	protected Label getDescriptionLabel(SectionInfo info)
	{
		return DESCRIPTION_LABEL;
	}
}
