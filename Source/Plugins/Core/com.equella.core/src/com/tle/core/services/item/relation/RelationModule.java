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

package com.tle.core.services.item.relation;

import com.google.inject.AbstractModule;
import com.tle.core.guice.PluginTrackerModule;

@SuppressWarnings("nls")
public class RelationModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		requestStaticInjection(RelationModify.class);
		install(new RelationTrackerModule());
	}

	public static class RelationTrackerModule extends PluginTrackerModule
	{
		@Override
		protected String getPluginId()
		{
			return "com.tle.core.services.item.relation";
		}

		@Override
		protected void configure()
		{
			bindTracker(RelationListener.class, "relationListener", "bean");
		}
	}
}
