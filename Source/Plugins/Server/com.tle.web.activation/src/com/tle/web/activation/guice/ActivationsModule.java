package com.tle.web.activation.guice;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.activation.ActivationResultsModifier;
import com.tle.web.activation.filter.FilterByActivationDateRangeSection;
import com.tle.web.activation.filter.FilterByActivationStatusSection;
import com.tle.web.activation.filter.FilterByCourseSection;
import com.tle.web.activation.section.ActivationResultsSection;
import com.tle.web.activation.section.ActivationSelectionSection;
import com.tle.web.activation.section.RootActivationSection;
import com.tle.web.search.filter.FilterByOwnerSection;
import com.tle.web.search.filter.SimpleResetFiltersQuerySection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.search.sort.SortOptionsSection;

@SuppressWarnings("nls")
public class ActivationsModule extends AbstractSearchModule
{
	@Override
	protected void configure()
	{
		super.configure();
		install(new TrackerModule());
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(ActivationSelectionSection.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(SortOptionsSection.class);
		node.child(FilterByActivationStatusSection.class);
		node.child(FilterByCourseSection.class);
		node.child(FilterByOwnerSection.class);
		node.child(FilterByActivationDateRangeSection.class);
	}

	@Override
	protected NodeProvider getRootNode()
	{
		return node(RootActivationSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(SimpleResetFiltersQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(ActivationResultsSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/access/activations";
	}

	public static class TrackerModule extends PluginTrackerModule
	{
		@Override
		protected void configure()
		{
			bindTracker(ActivationResultsModifier.class, "resultsModifier", "bean");
		}
	}
}
