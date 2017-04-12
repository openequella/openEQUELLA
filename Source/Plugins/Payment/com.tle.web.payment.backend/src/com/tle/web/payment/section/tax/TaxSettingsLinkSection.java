package com.tle.web.payment.section.tax;

import static com.tle.core.payment.PaymentConstants.PRIV_CREATE_TAX;
import static com.tle.core.payment.PaymentConstants.PRIV_DELETE_TAX;
import static com.tle.core.payment.PaymentConstants.PRIV_EDIT_TAX;

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
public class TaxSettingsLinkSection extends AbstractEntitySettingsLinkSection<Object>
{
	@PlugKey("tax.setting.title")
	private static Label TITLE_LABEL;
	@PlugKey("tax.setting.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private TLEAclManager aclService;

	public static HtmlLinkState getShowTaxesLink(SectionInfo info)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = info.createForward("/access/tax.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(PRIV_DELETE_TAX, PRIV_EDIT_TAX, PRIV_CREATE_TAX).isEmpty();
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return getShowTaxesLink(info);
	}

	@Override
	protected Label getDescriptionLabel(SectionInfo info)
	{
		return DESCRIPTION_LABEL;
	}
}
