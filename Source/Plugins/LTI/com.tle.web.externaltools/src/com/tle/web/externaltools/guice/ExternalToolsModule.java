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

package com.tle.web.externaltools.guice;

import com.google.inject.name.Names;
import com.tle.core.config.guice.OptionalConfigModule;
import com.tle.web.externaltools.section.ExternalToolContributeSection;
import com.tle.web.externaltools.section.RootExternalToolsSection;
import com.tle.web.externaltools.section.ShowExternalToolsSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class ExternalToolsModule extends SectionsModule
{

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("externalToolsTree")).toProvider(externalToolsTree());
		install(new ExternalToolsProps());
	}

	private NodeProvider externalToolsTree()
	{
		NodeProvider node = node(RootExternalToolsSection.class);
		node.innerChild(ExternalToolContributeSection.class);
		node.child(ShowExternalToolsSection.class);
		return node;
	}

	public static class ExternalToolsProps extends OptionalConfigModule
	{
		@Override
		protected void configure()
		{
			bindProp("external.tool.contact.email", "EXTERNAL_TOOL_CONTACT_EMAIL_NOT_CONFIGURED");
		}
	}
}

