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
