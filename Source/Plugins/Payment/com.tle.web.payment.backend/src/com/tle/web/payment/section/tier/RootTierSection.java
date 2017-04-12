package com.tle.web.payment.section.tier;

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

@Bind
public class RootTierSection extends AbstractRootEntitySection<OneColumnLayoutModel>
{
	@PlugKey("tier.setting.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.error.license")
	private static Label LABEL_ERROR_LICENSE;

	@Inject
	private TLEAclManager aclService;

	@Override
	protected boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PaymentConstants.PRIV_CREATE_TIER,
			PaymentConstants.PRIV_EDIT_TIER).isEmpty();
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return TierSettingsLinkSection.getShowTiersLink(info);
	}

}
