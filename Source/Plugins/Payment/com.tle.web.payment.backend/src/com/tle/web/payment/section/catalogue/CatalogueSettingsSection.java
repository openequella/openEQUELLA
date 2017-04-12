package com.tle.web.payment.section.catalogue;

import static com.tle.core.payment.PaymentConstants.PRIV_CREATE_CATALOGUE;
import static com.tle.core.payment.PaymentConstants.PRIV_EDIT_CATALOGUE;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractEntitySettingsLinkSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@SuppressWarnings("nls")
@Bind
public class CatalogueSettingsSection extends AbstractEntitySettingsLinkSection<Object>
{
	@PlugKey("catalogue.setting.title")
	private static Label TITLE_LABEL;
	@PlugKey("catalogue.setting.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private TLEAclManager aclService;

	public static HtmlLinkState getShowCataloguesLink(SectionInfo info)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = info.createForward("/access/catalogue.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PRIV_CREATE_CATALOGUE, PRIV_EDIT_CATALOGUE).isEmpty();
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return getShowCataloguesLink(info);
	}

	@Override
	protected Label getDescriptionLabel(SectionInfo info)
	{
		return DESCRIPTION_LABEL;
	}
}
