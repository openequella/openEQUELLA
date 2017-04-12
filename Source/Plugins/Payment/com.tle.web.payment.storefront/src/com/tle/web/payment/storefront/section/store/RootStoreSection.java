package com.tle.web.payment.storefront.section.store;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.security.TLEAclManager;
import com.tle.web.entities.section.AbstractRootEntitySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class RootStoreSection extends AbstractRootEntitySection<OneColumnLayoutModel>
{
	@PlugKey("store.page.title")
	private static Label TITLE_LABEL;

	@Inject
	private TLEAclManager aclService;

	@Override
	protected boolean canView(SectionInfo info)
	{
		return !aclService.filterNonGrantedPrivileges(StoreFrontConstants.PRIV_CREATE_STORE,
			StoreFrontConstants.PRIV_EDIT_STORE, StoreFrontConstants.PRIV_DELETE_STORE).isEmpty();
	}

	@Override
	protected Label getTitleLabel(SectionInfo info)
	{
		return TITLE_LABEL;
	}

	@Override
	protected HtmlLinkState getShowEntitiesLink(SectionInfo info)
	{
		return StoreRegisterSettingsLinkSection.getShowStoresLink(info);
	}

}
