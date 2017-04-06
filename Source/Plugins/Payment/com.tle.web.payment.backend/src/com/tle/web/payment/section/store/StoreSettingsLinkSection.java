package com.tle.web.payment.section.store;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.StoreSettingsPrivilegeTreeProvider;
import com.tle.core.system.LicenseService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.AbstractParentSettingsSection;

@Bind
@SuppressWarnings("nls")
public class StoreSettingsLinkSection extends AbstractParentSettingsSection<Object>
{
	@PlugKey("store.settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("store.settings.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private StoreSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private LicenseService licenseService;

	@Override
	public boolean canView(SectionInfo info)
	{
		return securityProvider.isAuthorised()
			&& licenseService.isFeatureEnabled(PaymentConstants.LICENSE_FEATURE_CONTENT_EXCHANGE);
	}

	@Override
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		HtmlLinkState state = new HtmlLinkState();
		SectionInfo fwd = context.createForward("/access/storesettings.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return new Pair<HtmlLinkState, Label>(state, DESCRIPTION_LABEL);
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
