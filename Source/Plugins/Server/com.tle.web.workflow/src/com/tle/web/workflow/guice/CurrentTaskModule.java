package com.tle.web.workflow.guice;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.workflow.tasks.CurrentTaskSection;
import com.tle.web.workflow.tasks.comments.CommentsSection;

public class CurrentTaskModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("$CURRENTTASK$")).toProvider(currentTaskTree());
	}

	private NodeProvider currentTaskTree()
	{
		return node(CurrentTaskSection.class).child(CommentsSection.class);
	}
}