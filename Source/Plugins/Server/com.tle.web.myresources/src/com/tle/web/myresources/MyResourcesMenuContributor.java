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

package com.tle.web.myresources;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
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
