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

package com.tle.web.integration.guice;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.integration.Integration;
import com.tle.web.integration.IntegrationActionInfo;
import com.tle.web.integration.IntegrationSection;
import com.tle.web.integration.IntegrationSessionData;
import com.tle.web.integration.IntegrationSessionExtension;
import com.tle.web.integration.SingleSignonAction;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.selection.section.RootSelectionSection.Layout;

@SuppressWarnings("nls")
@NonNullByDefault
public class IntegrationModule extends SectionsModule
{
	public static final String SELECT_OR_ADD_DEFAULT_ACTION = "selectOrAdd";

	@Override
	protected void configure()
	{
		ActionInfoProvider contribute = new ActionInfoProvider("contribute", "contribute");
		contribute.option("allContributionCollections", "true");
		contribute.option("allRemoteRepos", "true");
		contribute.option("home", "contribute");

		ActionInfoProvider searchResources = new ActionInfoProvider("searchResources", "search");
		searchResources.option("allCollections", "true");
		searchResources.option("allPowerSearches", "true");
		searchResources.option("allDynamicCollections", "true");
		searchResources.option("home", "search");

		ActionInfoProvider selectOrAdd = new ActionInfoProvider(SELECT_OR_ADD_DEFAULT_ACTION, "home");
		selectOrAdd.option("allCollections", "true");
		selectOrAdd.option("allPowerSearches", "true");
		selectOrAdd.option("allContributionCollections", "true");
		selectOrAdd.option("allDynamicCollections", "true");
		selectOrAdd.option("allRemoteRepos", "true");
		selectOrAdd.option("home", "home");

		ActionInfoProvider searchThin = new ActionInfoProvider("searchThin", "skinnysearch");
		searchThin.option("allCollections", "true");
		searchThin.option("allPowerSearches", "true");
		searchThin.option("allDynamicCollections", "true");
		searchThin.option("allRemoteRepositories", "false");
		searchThin.option("home", "skinnysearch");
		searchThin.option("layout", Layout.SKINNY.toString());

		ActionInfoProvider courseEnhanced = new ActionInfoProvider("structured", "coursesearch");
		courseEnhanced.option("allCollections", "true");
		courseEnhanced.option("allContributionCollections", "true");
		courseEnhanced.option("allPowerSearches", "true");
		courseEnhanced.option("allDynamicCollections", "true");
		courseEnhanced.option("allRemoteRepositories", "true");
		courseEnhanced.option("layout", Layout.COURSE.toString());
		courseEnhanced.option("home", "coursesearch");

		bind(Object.class).annotatedWith(Names.named("action-contribute")).toProvider(contribute);
		bind(Object.class).annotatedWith(Names.named("action-searchResources")).toProvider(searchResources);
		bind(Object.class).annotatedWith(Names.named("action-selectoradd")).toProvider(selectOrAdd);
		bind(Object.class).annotatedWith(Names.named("action-searchthin")).toProvider(searchThin);
		bind(Object.class).annotatedWith(Names.named("action-structured")).toProvider(courseEnhanced);

		bind(Object.class).annotatedWith(Names.named("/signon")).toProvider(node(SingleSignonAction.class));

		bind(SectionTree.class).annotatedWith(Names.named("integrationTree")).toProvider(tree(node(IntegrationSection.class)));

		install(new TrackerModule());
	}

	public static class ActionInfoProvider implements Provider<IntegrationActionInfo>
	{
		private final String selectable;
		private final String name;
		private final Builder<String, Object> map = ImmutableMap.builder();

		public ActionInfoProvider(String name, String selectable)
		{
			this.name = name;
			this.selectable = selectable;
		}

		private ActionInfoProvider option(String option, Object value)
		{
			map.put(option, value);
			return this;
		}

		@Override
		public IntegrationActionInfo get()
		{
			IntegrationActionInfo info = new IntegrationActionInfo();
			info.setName(name);
			info.setSelectable(selectable);
			info.setOptionMap(map.build());
			return info;
		}
	}

	public static class TrackerModule extends PluginTrackerModule
	{
		@Override
		protected String getPluginId()
		{
			return "com.tle.web.integration";
		}

		@Override
		protected void configure()
		{
			bindTracker(IntegrationSessionExtension.class, "integrationSession", "class");
			bindTracker(IntegrationActionInfo.class, "sso-action", "class").setIdParam("name");
			final TypeLiteral<Integration<? extends IntegrationSessionData>> integType = new TypeLiteral<Integration<? extends IntegrationSessionData>>()
			{
				// No comment
			};
			bindTracker(integType.getType(), "integration", "class").setIdParam("id");
		}
	}
}
