package com.tle.web.payment.section.region;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.security.TLEAclManager;
import com.tle.core.system.LicenseService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.entities.section.AbstractRootEntitySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class RootRegionSection extends AbstractRootEntitySection<OneColumnLayoutModel>
{
	@PlugKey("region.setting.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.error.license")
	private static Label LABEL_ERROR_LICENSE;

	@Inject
	private TLEAclManager aclService;
	@Inject
	private LicenseService licenseService;

	@Override
	protected boolean canView(SectionInfo info)
	{
		if( !licenseService.isFeatureEnabled(PaymentConstants.LICENSE_FEATURE_CONTENT_EXCHANGE) )
		{
			throw new AccessDeniedException(LABEL_ERROR_LICENSE.getText());
		}
		return !aclService.filterNonGrantedPrivileges(PaymentConstants.PRIV_CREATE_REGION,
			PaymentConstants.PRIV_EDIT_REGION).isEmpty();
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return RegionSettingsSection.getShowRegionsLink(info);
	}

}
