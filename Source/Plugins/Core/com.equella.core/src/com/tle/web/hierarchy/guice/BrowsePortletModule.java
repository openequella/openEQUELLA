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

package com.tle.web.hierarchy.guice;

import com.google.inject.name.Names;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.web.hierarchy.portlet.renderer.BrowsePortletRenderer;
import com.tle.web.sections.Section;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.selection.home.sections.SelectionPortletRendererWrapper;

@SuppressWarnings("nls")
public class BrowsePortletModule extends SectionsModule
{
	private static String KEY_PFX = AbstractPluginService.getMyPluginId(BrowsePortletModule.class)+".";

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("com.tle.web.hierarchy.portlet.browseSelectionPortal"))
			.toProvider(browsePortletTree());
	}

	private NodeProvider browsePortletTree()
	{
		NodeProvider node = new NodeProvider(SelectionPortletRendererWrapper.class)
		{
			@Override
			protected void customize(Section section)
			{
				SelectionPortletRendererWrapper sprw = (SelectionPortletRendererWrapper) section;
				sprw.setPortletNameKey(KEY_PFX+"portlet.browse.name");
				sprw.setPortletType("browse");
			}
		};

		node.child(BrowsePortletRenderer.class);

		return node;
	}
}
