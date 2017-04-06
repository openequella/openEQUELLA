package com.tle.web.cloud.search.section;

import javax.inject.Inject;

import com.dytech.edge.web.WebConstants;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.cloud.service.CloudService;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.UrlService;
import com.tle.core.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.login.LogonSection;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class RootCloudSearchSection extends ContextableSearchSection<ContextableSearchSection.Model>
{
	public static final String SEARCH_SESSIONKEY = "cloudSearchContext";

	@PlugKey("search.title")
	private static Label LABEL_TITLE;
	@PlugKey("search.error.clouddisabled")
	private static Label LABEL_ERROR_CLOUD_DISABLED;

	@Inject
	private CloudService cloudService;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private UrlService urlService;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !cloudService.isCloudy() )
		{
			throw new AccessDeniedException(LABEL_ERROR_CLOUD_DISABLED.getText());
		}

		// Anonymous search privilege if it exists extends to cloud search
		if( aclManager.filterNonGrantedPrivileges(WebConstants.SEARCH_PAGE_PRIVILEGE).isEmpty() )
		{
			if( CurrentUser.isGuest() )
			{
				LogonSection.forwardToLogon(context,
					urlService.removeInstitution(context.getPublicBookmark().getHref()),
					LogonSection.STANDARD_LOGON_PATH);
				return null;
			}
			throw new AccessDeniedException(CurrentLocale.get("com.tle.web.searching.missingprivileges",
				WebConstants.SEARCH_PAGE_PRIVILEGE));
		}

		return super.renderHtml(context);
	}

	@Override
	protected String getSessionKey()
	{
		return SEARCH_SESSIONKEY;
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return selectionService.getCurrentSession(info) != null ? super.getDefaultLayout(info)
			: ContentLayout.ONE_COLUMN;
	}
}
