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

package com.tle.web.selection.home.guice;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.selection.home.recentsegments.ContributionsSegment;
import com.tle.web.selection.home.recentsegments.SelectionsSegment;
import com.tle.web.selection.home.sections.RootSelectionHomeSection;
import com.tle.web.selection.home.sections.SelectionRecentSection;
import com.tle.web.selection.home.sections.ShowSelectionHomePortalsSection;
import com.tle.web.selection.section.SelectionSummarySection;

public class SelectionHomeModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		NodeProvider node = node(RootSelectionHomeSection.class);
		NodeProvider portalSection = node(ShowSelectionHomePortalsSection.class).placeHolder("SELECTION_HOME");
		NodeProvider recent = node(SelectionRecentSectionHome.class).placeHolder("SELECTION_HOME_RECENT");
		recent.child(ContributionsSegment.class);
		recent.child(SelectionsSegment.class);
		portalSection.child(recent);
		portalSection.child(SelectionSummarySectionHome.class);
		node.child(portalSection);
		bind(Object.class).annotatedWith(Names.named("/access/selection/home")).toProvider(node);
	}

	public static class SelectionSummarySectionHome extends SelectionSummarySection
	{
		@SuppressWarnings("nls")
		public SelectionSummarySectionHome()
		{
			setLayout("{column: 'right', order: 1}");
			setFinishedInBox(true);
		}
	}

	public static class SelectionRecentSectionHome extends SelectionRecentSection
	{
		@SuppressWarnings("nls")
		public SelectionRecentSectionHome()
		{
			setLayout("{column:'right', order: 100}");
		}
	}
}
