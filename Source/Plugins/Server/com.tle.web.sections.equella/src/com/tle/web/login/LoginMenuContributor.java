package com.tle.web.login;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.section.MenuContributor;

@Bind
@Singleton
@SuppressWarnings("nls")
public class LoginMenuContributor implements MenuContributor
{
	private static final Label LABEL = new KeyLabel(ResourcesService.getResourceHelper(LoginMenuContributor.class).key(
		"menu.login"));
	private static final String ICON_URL = ResourcesService.getResourceHelper(LoginMenuContributor.class).url(
		"images/menu-icon-login.png");

	@Inject
	private UrlService urlService;

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		String relUrl = urlService.removeInstitution(info.getPublicBookmark().getHref());
		HtmlLinkState hls = new HtmlLinkState(LogonSection.forwardToLogonBookmark(info, relUrl,
			urlService.institutionalise(LogonSection.STANDARD_LOGON_PATH)));
		hls.setLabel(LABEL);
		MenuContribution mc = new MenuContribution(hls, ICON_URL, 1, 1);
		return Collections.singletonList(mc);
	}

	@Override
	public void clearCachedData()
	{
		// Nothing is cached
	}
}
