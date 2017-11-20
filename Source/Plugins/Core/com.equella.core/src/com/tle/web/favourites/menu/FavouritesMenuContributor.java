/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.favourites.menu;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
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
