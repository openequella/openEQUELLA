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

package com.tle.web.selection.guice;

import com.google.inject.name.Names;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.sections.equella.impl.ModalErrorSection;
import com.tle.web.selection.SelectableAttachment;
import com.tle.web.selection.SelectableInterface;
import com.tle.web.selection.SelectionNavAction;
import com.tle.web.selection.section.CourseListSection;
import com.tle.web.selection.section.RootSelectionSection;
import com.tle.web.selection.section.SelectionCheckoutSection;
import com.tle.web.selection.section.SkinnySelectionProviderSection;
import com.tle.web.template.section.HelpAndScreenOptionsSection;
import com.tle.web.template.section.ServerMessageSection;

@SuppressWarnings("nls")
public class SelectionModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/selection/checkout")).toProvider(
			node(SelectionCheckoutSection.class));
		bind(SectionTree.class).annotatedWith(Names.named("selectionTree")).toProvider(tree(selectionTree()));

		install(new TrackerModule());
	}

	private NodeProvider selectionTree()
	{
		NodeProvider node = node(RootSelectionSection.class);
		node.child(HelpAndScreenOptionsSection.class);
		node.child(ServerMessageSection.class);

		// would be good not to have these hardcoded here
		node.child(CourseListSection.class);
		node.child(SkinnySelectionProviderSection.class);

		node.innerChild(ModalErrorSection.class);
		return node;
	}

	private static class TrackerModule extends PluginTrackerModule
	{
		@Override
		protected String getPluginId()
		{
			return "com.tle.web.selection";
		}

		@Override
		protected void configure()
		{
			bindTracker(SelectionNavAction.class, "selectionNavActions", "class").setIdParam("type");
			bindTracker(SelectableInterface.class, "selectable", "selectBean").setIdParam("id");
			bindTracker(SelectableAttachment.class, "selectableAttachment", "class");
		}
	}
}
