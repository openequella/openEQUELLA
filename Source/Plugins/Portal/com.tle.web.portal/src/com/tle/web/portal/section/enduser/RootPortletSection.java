package com.tle.web.portal.section.enduser;

import javax.inject.Inject;

import com.dytech.edge.web.WebConstants;
import com.tle.annotation.Nullable;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.UrlService;
import com.tle.core.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.login.LogonSection;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.CombinedLayout;
import com.tle.web.sections.equella.layout.CombinedLayout.CombinedModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author aholland
 */
public class RootPortletSection extends CombinedLayout<CombinedModel>
{
	@PlugKey("page.portal.title")
	private static Label TITLE_LABEL;

	@Inject
	private TLEAclManager aclManager;
	@Inject
	private UrlService urlService;

	@Override
	public Class<CombinedModel> getModelClass()
	{
		return CombinedModel.class;
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		CombinedModel model = getModel(info);
		SectionId modalSection = model.getModalSection();
		if( modalSection != null )
		{
			SectionId section = info.getSectionForId(modalSection);
			if( section instanceof ModalPortletSection )
			{
				((ModalPortletSection) section).addBreadcrumbsAndTitle(info, decorations, crumbs);
				return;
			}
		}
		decorations.setTitle(TITLE_LABEL);
		decorations.setContentBodyClass("dashboard"); //$NON-NLS-1$
	}

	@Nullable
	@Override
	protected TemplateResult getTemplateResult(RenderEventContext info)
	{
		if( aclManager.filterNonGrantedPrivileges(WebConstants.DASHBOARD_PAGE_PRIVILEGE).isEmpty() )
		{
			if( CurrentUser.isGuest() )
			{
				LogonSection.forwardToLogon(info, urlService.removeInstitution(info.getPublicBookmark().getHref()),
					LogonSection.STANDARD_LOGON_PATH);
				return null;
			}
			throw new AccessDeniedException(CurrentLocale.get("com.tle.web.portal.missingprivileges",
				WebConstants.DASHBOARD_PAGE_PRIVILEGE));
		}

		getModel(info).setReceiptSpanBothColumns(true);

		return super.getTemplateResult(info);
	}
}
