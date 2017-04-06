package com.tle.web.oauth.section;

import java.util.Arrays;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.oauth.OAuthConstants;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@Bind
public class RootOAuthSection extends OneColumnLayout<OneColumnLayoutModel>
{
	@PlugKey("oauth.page.title")
	private static Label TITLE_LABEL;
	@PlugKey("oauth.error.noaccess")
	private static Label LABEL_NOACCESS;

	@Inject
	private TLEAclManager aclService;

	private boolean canView()
	{
		return !aclService.filterNonGrantedPrivileges(
			Arrays.asList(OAuthConstants.PRIV_EDIT_OAUTH_CLIENT, OAuthConstants.PRIV_CREATE_OAUTH_CLIENT)).isEmpty();
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView() )
		{
			throw new AccessDeniedException(LABEL_NOACCESS.getText());
		}

		return super.renderHtml(context);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		OneColumnLayoutModel model = getModel(info);
		SectionId modalSection = model.getModalSection();
		crumbs.add(SettingsUtils.getBreadcrumb());

		if( modalSection != null )
		{
			crumbs.add(OAuthSettingsSection.getShowOAuthLink(info));

			SectionId section = info.getSectionForId(modalSection);
			if( section instanceof ModalOAuthSection )
			{
				((ModalOAuthSection) section).addBreadcrumbsAndTitle(info, decorations, crumbs);
				return;
			}
		}
		decorations.setTitle(TITLE_LABEL);
		decorations.setContentBodyClass("oauth");
	}

	@Override
	public Class<OneColumnLayoutModel> getModelClass()
	{
		return OneColumnLayoutModel.class;
	}
}
