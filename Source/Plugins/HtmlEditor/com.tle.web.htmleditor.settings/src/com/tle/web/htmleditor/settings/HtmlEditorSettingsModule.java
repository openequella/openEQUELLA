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
