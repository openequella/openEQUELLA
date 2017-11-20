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

package com.tle.web.institution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Singleton;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.registry.handler.CollectInterfaceHandler;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.section.MenuContributor;

@Bind
@Singleton
public class InstitutionMenuContributor implements MenuContributor
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(InstitutionMenuContributor.class);

	@Override
	public List<MenuContribution> getMenuContributions(SectionInfo info)
	{
		final List<Tabable> tabs = new CollectInterfaceHandler<Tabable>(Tabable.class).getAllImplementors(info);
		if( Check.isEmpty(tabs) )
		{
			return null;
		}

		final Map<String, JSHandler> tabHandlers = new HashMap<String, JSHandler>();
		for( Tabable tab : tabs )
		{
			List<Tab> tabList = tab.getTabs(info);
			if( !tabList.isEmpty() )
			{
				Tab t = tabList.get(0);
				String id = t.getId();
				JSHandler handler = t.getClickHandler();
				tabHandlers.put(id, handler);
			}
		}

		if( Check.isEmpty(tabHandlers) )
		{
			return null;
		}

		final List<MenuContribution> mcs = new ArrayList<MenuContribution>();

		add(mcs, tabHandlers, "isadmin", 1, 1, "institutions.menu", "images/institutions-menu-icon.png");
		add(mcs, tabHandlers, "isii", 1, 2, "import.menu", "images/import-menu-icon.png");
		add(mcs, tabHandlers, "isdt", 1, 3, "databases.menu", "images/databases-menu-icon.png");

		add(mcs, tabHandlers, "isservertab", 2, 1, "settings.menu", "images/settings-menu-icon.png");

		add(mcs, tabHandlers, "isclusternodes", 3, 1, "health.menu", "images/clusterhealth-menu-icon.png");
		add(mcs, tabHandlers, "isthreaddump", 3, 2, "threaddump.menu", "images/threaddump-menu-icon.png");

		return mcs;
	}

	private void add(List<MenuContribution> mcs, Map<String, JSHandler> tabHandlers, String handlerName,
		int groupPriority, int linkPriority, String labelKey, String iconPath)
	{
		JSHandler handler = tabHandlers.get(handlerName);
		if( handler != null )
		{
			MenuContribution m = new MenuContribution(
				new HtmlLinkState(new KeyLabel(RESOURCES.key(labelKey)), handler), RESOURCES.url(iconPath),
				groupPriority, linkPriority);
			mcs.add(m);
		}
	}

	@Override
	public void clearCachedData()
	{
		// Nothing to do here
	}
}