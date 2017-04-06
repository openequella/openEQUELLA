package com.tle.web.payment.storefront.section.store;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractEntitySettingsLinkSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@SuppressWarnings("nls")
@Bind
public class StoreRegisterSettingsLinkSection extends AbstractEntitySettingsLinkSection<Object>
{
	@PlugKey("store.setting.title")
	private static Label TITLE_LABEL;
	@PlugKey("store.setting.description")
	private static Label DESCRIPTION_LABEL;

	@Inject
	private TLEAclManager aclService;

	@Override
	public boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(StoreFrontConstants.PRIV_CREATE_STORE,
			StoreFrontConstants.PRIV_EDIT_STORE, StoreFrontConstants.PRIV_DELETE_STORE).isEmpty();
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return getShowStoresLink(info);
	}

	public static HtmlLinkState getShowStoresLink(SectionInfo info)
	{
		final HtmlLinkState state = new HtmlLinkState();
		final SectionInfo fwd = info.createForward("/access/store.do");
		state.setBookmark(fwd.getPublicBookmark());
		state.setLabel(TITLE_LABEL);
		return state;
	}

	@Override
	protected Label getDescriptionLabel(SectionInfo info)
	{
		return DESCRIPTION_LABEL;
	}
}
