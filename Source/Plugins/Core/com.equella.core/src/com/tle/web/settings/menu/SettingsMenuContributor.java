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

package com.tle.web.settings.menu;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.section.MenuContributor;

@Bind
@Singleton
@SuppressWarnings("nls")
public class SettingsMenuContributor implements MenuContributor
{
	private static final Label LABEL_KEY = new KeyLabel(
		ResourcesService.getResourceHelper(SettingsMenuContributor.class).key("menu"));
	private static final String ICON_PATH = ResourcesService.getResourceHelper(SettingsMenuContributor.class)
		.url("images/menu-icon-settings.png");
	private static final String SESSION_KEY = "SETTINGS-MENU";

	@Inject
	private UserSessionService userSessionService;

	private PluginTracker<ViewableChildInterface> extensionTracker;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		extensionTracker = new PluginTracker<ViewableChildInterface>(pluginService, "com.tle.web.settings", "settingsExtension",
			null);
		extensionTracker.setBeanKey("class");
	}

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		Boolean showSettings = userSessionService.getAttribute(SESSION_KEY);
		if( showSettings == null )
		{
			showSettings = canView(info);
			userSessionService.setAttribute(SESSION_KEY, showSettings);
		}

		List<MenuContribution> mcs = new ArrayList<MenuContribution>();
		if( showSettings )
		{
			HtmlLinkState hls = new HtmlLinkState(SettingsUtils.SETTINGS_BOOKMARK);
			hls.setLabel(LABEL_KEY);

			MenuContribution mc = new MenuContribution(hls, ICON_PATH, 30, 30);
			mcs.add(mc);
		}

		return mcs;
	}

	private boolean canView(SectionInfo info)
	{
		for( ViewableChildInterface bean : extensionTracker.getBeanList() )
		{
			if( bean.canView(info) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void clearCachedData()
	{
		userSessionService.removeAttribute(SESSION_KEY);
	}
}
