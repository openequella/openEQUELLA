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
		protected String getPluginId()
		{
			return "com.tle.web.activation";
		}

		@Override
		protected void configure()
		{
			bindTracker(ActivationResultsModifier.class, "resultsModifier", "bean");
		}
	}
}
