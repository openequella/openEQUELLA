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

package com.tle.core.item.guice;

import com.google.inject.AbstractModule;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.item.ItemIdExtension;
import com.tle.core.item.helper.AbstractHelper;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.scripting.WorkflowScriptObjectContributor;
import com.tle.core.item.service.ItemResolverExtension;

/**
 * @author Aaron
 *
 */
public class ItemModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		install(new TrackerModule());
	}

	public static class TrackerModule extends PluginTrackerModule
	{
		@Override
		protected String getPluginId()
		{
			return "com.tle.core.item";
		}

		@Override
		protected void configure()
		{
			bindTracker(ItemResolverExtension.class, "itemResolver", "bean").setIdParam("id");
			bindTracker(ItemIdExtension.class, "itemIdExtension", "bean").setIdParam("id");
			bindTracker(AbstractHelper.class, "itemHelpers", "bean").orderByParameter("order");
			bindTracker(WorkflowOperation.class, "operation", "class").orderByParameter("order");
			bindTracker(WorkflowScriptObjectContributor.class, "scriptObjects", "class").setIdParam("id");
		}
	}
}
