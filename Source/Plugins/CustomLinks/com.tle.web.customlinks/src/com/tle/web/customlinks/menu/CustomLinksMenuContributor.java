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

package com.tle.web.customlinks.menu;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.Check;
import com.tle.common.customlinks.entity.CustomLink;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.customlinks.service.CustomLinkService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.customlinks.CustomLinkLabel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.template.section.MenuContributor;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CustomLinksMenuContributor implements MenuContributor
{
	private static final String SESSION_KEY = "CUSTOM-LINKS-MENU";

	@Inject
	private CustomLinkService linkService;
	@Inject
	private UserSessionService userSessionService;
	@Inject
	private InstitutionService institutionService;

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		List<CustomLinkLabel> cachedLinks = userSessionService.getAttribute(SESSION_KEY);

		if( cachedLinks == null )
		{
			cachedLinks = new ArrayList<CustomLinkLabel>();

			List<CustomLink> links = linkService.listLinksForUser();

			for( CustomLink lk : links )
			{
				String name = CurrentLocale.get(lk.getName(), "");
				boolean newWindow = lk.getAttribute("newWindow", false);
				String url = lk.getUrl();

				String iconUrl = null;
				String file = lk.getAttribute("fileName");
				if( !Check.isEmpty(file) )
				{
					try
					{
						file = new URI(null, null, file, null).toString();
					}
					catch( URISyntaxException e )
					{
						// nothing
					}

					iconUrl = institutionService.institutionalise("entity/" + lk.getId() + "/" + file);
				}

				cachedLinks.add(new CustomLinkLabel(name, url, newWindow, iconUrl));
			}

			userSessionService.setAttribute(SESSION_KEY, cachedLinks);
		}

		List<MenuContribution> mcs = new ArrayList<MenuContribution>();

		int pri = 0;
		for( CustomLinkLabel aLink : cachedLinks )
		{
			mcs.add(newLink(aLink.getName(), aLink.getUrl(), pri++, aLink.isNewWindow(), aLink.getIconUrl()));
		}

		return mcs;
	}

	private MenuContribution newLink(String name, String url, int order, boolean newWindow, String iconUrl)
	{
		final String target = newWindow ? "_blank" : null;

		HtmlLinkState link = new HtmlLinkState(new SimpleBookmark(url));
		link.setLabel(new TextLabel(name));
		link.setTarget(target);

		return new MenuContribution(link, iconUrl, 20, order);
	}

	@Override
	public void clearCachedData()
	{
		userSessionService.removeAttribute(SESSION_KEY);
	}
}
