package com.tle.web.portal.standard.guice;

import com.google.inject.name.Names;
import com.tle.web.portal.standard.editor.tabs.ClientScriptTab;
import com.tle.web.portal.standard.editor.tabs.FreemarkerTab;
import com.tle.web.portal.standard.editor.tabs.RootEditorTabs;
import com.tle.web.portal.standard.editor.tabs.ServerScriptTab;
import com.tle.web.sections.equella.guice.SectionsModule;

public class ScriptedPortletEditorModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("freemarkerPortletEditorTabs")).toProvider(scriptedTree());
	}

	private NodeProvider scriptedTree()
	{
		NodeProvider node = node(RootEditorTabs.class);
		node.child(FreemarkerTab.class);
		node.child(ServerScriptTab.class);
		node.child(ClientScriptTab.class);
		return node;
	}
}
