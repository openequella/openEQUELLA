package com.tle.web.activation;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.section.MenuContributor;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ActivationsMenuContributor implements MenuContributor
{
	private static final Label LABEL_KEY = new KeyLabel(ResourcesService.getResourceHelper(
		ActivationsMenuContributor.class).key("menu.activations"));
	private static final String ICON_PATH = ResourcesService.getResourceHelper(ActivationsMenuContributor.class).url(
		"images/menu-icon-activations.png");

	private static final String SESSION_KEY = "ACTIVATIONS-MENU";

	@Inject
	private ActivationsPrivilegeTreeProvider securityProvider;
	@Inject
	private UserSessionService userSessionService;

	@Override
	public void clearCachedData()
	{
		userSessionService.removeAttribute(SESSION_KEY);
	}

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		Boolean show = userSessionService.getAttribute(SESSION_KEY);

		if( show == null )
		{
			show = securityProvider.isAuthorised();
			userSessionService.setAttribute(SESSION_KEY, show);
		}

		if( show )
		{
			// TODO: We should be generating a bookmark to the section rather
			// than hard-coding the URL

			HtmlLinkState hls = new HtmlLinkState(new SimpleBookmark("access/activations.do"));
			hls.setLabel(LABEL_KEY);
			MenuContribution mc = new MenuContribution(hls, ICON_PATH, 30, 15);
			return Collections.singletonList(mc);
		}
		else
		{
			return Collections.emptyList();
		}
	}
}
