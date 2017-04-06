package com.tle.web.controls.mypages;

import com.google.inject.name.Names;
import com.tle.mypages.web.section.MyPagesContributeSection;
import com.tle.mypages.web.section.MyPagesEditorSection;
import com.tle.mypages.web.section.RootMyPagesSection;
import com.tle.web.sections.SectionNode;
import com.tle.web.sections.equella.guice.SectionsModule;

public class MyPagesControlModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		NodeProvider node = node(RootMyPagesSection.class);

		NodeProvider handlerNode = node(MyPagesContributeSection.class);
		node.child(handlerNode);

		handlerNode.child(MyPagesHandlerPageActionsSection.class);

		NodeProvider edNode = node(MyPagesEditorSection.class);
		edNode.child(MyPagesExtrasSection.class);
		handlerNode.child(edNode);

		bind(SectionNode.class).annotatedWith(Names.named("myPagesTree")).toProvider(node);
	}

}
