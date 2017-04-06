package com.tle.web.myresources;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentUser;
import com.tle.web.resources.PluginResourceHelper;
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
public class MyResourcesMenuContributor implements MenuContributor
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(MyResourcesMenuContributor.class);
	private static final Label LABEL_KEY = new KeyLabel(RESOURCES.key("menu"));
	private static final String ICON_PATH = RESOURCES.url("images/menu-icon.png");

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		if( CurrentUser.wasAutoLoggedIn() )
		{
			return Collections.emptyList();
		}

		HtmlLinkState hls = new HtmlLinkState(new SimpleBookmark("access/myresources.do"));
		hls.setLabel(LABEL_KEY);
		MenuContribution mc = new MenuContribution(hls, ICON_PATH, 1, 20);
		return Collections.singletonList(mc);
	}

	@Override
	public void clearCachedData()
	{
		// Nothing is cached
	}
}
