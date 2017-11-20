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

package com.tle.web.itemlist;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ItemListEntryFactoryExtension;
import com.tle.web.sections.equella.guice.SectionsModule;

public class ItemListModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		install(new Trackers());
	}

	private static class Trackers extends PluginTrackerModule
	{
		@Override
		protected String getPluginId()
		{
			return "com.tle.web.itemlist";
		}

		@Override
		protected void configure()
		{
			bindTracker(ItemlikeListEntryExtension.class, "itemListExtension", "bean").orderByParameter("order");
			bindTracker(ItemListEntryFactoryExtension.class, "itemListFactoryExtension", "bean");
		}
	}
}
