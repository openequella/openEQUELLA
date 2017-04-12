package com.tle.web.htmleditor.settings;

import com.google.inject.name.Names;
import com.tle.web.htmleditor.settings.section.HtmlEditorOptionsSection;
import com.tle.web.htmleditor.settings.section.HtmlEditorSettingsFrontPageSection;
import com.tle.web.htmleditor.settings.section.HtmlEditorStylesSection;
import com.tle.web.htmleditor.settings.section.HtmlEditorToolbarSection;
import com.tle.web.htmleditor.settings.section.RootHtmlEditorSettingsSection;
import com.tle.web.htmleditor.settings.section.ShowHtmlEditorPluginsSection;
import com.tle.web.sections.equella.guice.SectionsModule;

public class HtmlEditorSettingsModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("htmleditorTree")).toProvider(htmleditorTree());
	}

	private NodeProvider htmleditorTree()
	{
		NodeProvider root = node(RootHtmlEditorSettingsSection.class);
		NodeProvider front = root.child(HtmlEditorSettingsFrontPageSection.class);
		front.innerChild(ShowHtmlEditorPluginsSection.class);
		front.innerChild(HtmlEditorStylesSection.class);
		front.innerChild(HtmlEditorToolbarSection.class);
		front.innerChild(HtmlEditorOptionsSection.class);
		return root;
	}
}
