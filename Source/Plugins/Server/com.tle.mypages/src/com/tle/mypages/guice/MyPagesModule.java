package com.tle.mypages.guice;

import com.google.inject.name.Names;
import com.tle.mypages.web.section.MyPagesContributeSection;
import com.tle.mypages.web.section.MyPagesEditorSection;
import com.tle.mypages.web.section.MyPagesPageActionsSection;
import com.tle.mypages.web.section.MyPagesTitleSection;
import com.tle.mypages.web.section.RootMyPagesSection;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.guice.SectionsModule;

public class MyPagesModule extends SectionsModule
{
	private NodeProvider createEditorTree()
	{
		NodeProvider handlerNode = node(MyPagesContributeSection.class);
		handlerNode.child(MyPagesTitleSection.class);
		handlerNode.child(MyPagesPageActionsSection.class);
		NodeProvider editorNode = node(MyPagesEditorSection.class);
		handlerNode.child(editorNode);
		return handlerNode;
	}

	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		NodeProvider node = node(RootMyPagesSection.class);
		node.child(createEditorTree());
		bindNamed("/access/mypagesedit", node);
		bind(SectionTree.class).annotatedWith(Names.named("myPagesContentTree")).toProvider(tree(createEditorTree()));
	}
}
