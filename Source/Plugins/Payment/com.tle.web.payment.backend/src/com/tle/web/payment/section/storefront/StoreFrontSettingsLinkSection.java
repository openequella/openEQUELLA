package com.tle.web.payment.section.storefront;

import static com.tle.core.payment.PaymentConstants.PRIV_DELETE_STOREFRONT;
import static com.tle.core.payment.PaymentConstants.PRIV_EDIT_STOREFRONT;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractEntitySettingsLinkSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@SuppressWarnings("nls")
@Bind
public class StoreFrontSettingsLinkSection extends AbstractEntitySettingsLinkSection<Object>
{
	@PlugKey("storefront.setting.title")
	private static Label TITLE_LABEL;
	@PlugKey("storefront.setting.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private TLEAclManager aclService;

	public static HtmlLinkState getShowStoreFrontsLink(SectionInfo info)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = info.createForward("/access/storefronts.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PRIV_DELETE_STOREFRONT, PRIV_EDIT_STOREFRONT).isEmpty();
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return getShowStoreFrontsLink(info);
	}

	@Override
	protected Label getDescriptionLabel(SectionInfo info)
	{
		return DESCRIPTION_LABEL;
	}
}
