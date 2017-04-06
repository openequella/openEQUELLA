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
		bind(SectionTree.class).toProvider(tree(selectionTree()));

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
		protected void configure()
		{
			bindTracker(SelectionNavAction.class, "selectionNavActions", "class").setIdParam("type");
			bindTracker(SelectableInterface.class, "selectable", "selectBean").setIdParam("id");
			bindTracker(SelectableAttachment.class, "selectableAttachment", "class");
		}
	}
}
