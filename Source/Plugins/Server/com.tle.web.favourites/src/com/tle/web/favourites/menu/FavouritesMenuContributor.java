package com.tle.web.favourites.menu;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentUser;
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
public class FavouritesMenuContributor implements MenuContributor
{
	private static final Label LABEL_KEY = new KeyLabel(ResourcesService.getResourceHelper(
		FavouritesMenuContributor.class).key("menu.favourites"));
	private static final String ICON_PATH = ResourcesService.getResourceHelper(FavouritesMenuContributor.class).url(
		"images/menu-icon-favourites.png");

	@Override
	public void clearCachedData()
	{
		// Boom
	}

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		if( CurrentUser.wasAutoLoggedIn() )
		{
			return Collections.emptyList();
		}

		// TODO: We should be generating a bookmark to the section rather than
		// hard-coding the URL
		HtmlLinkState hls = new HtmlLinkState(new SimpleBookmark("access/favourites.do"));
		hls.setLabel(LABEL_KEY);
		MenuContribution mc = new MenuContribution(hls, ICON_PATH, 1, 2);
		return Collections.singletonList(mc);
	}
}
