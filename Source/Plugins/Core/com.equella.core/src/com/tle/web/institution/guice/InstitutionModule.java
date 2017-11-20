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

package com.tle.web.institution.guice;

import com.google.inject.name.Names;
import com.tle.core.config.guice.OptionalConfigModule;
import com.tle.web.institution.AutoTestSetupSection;
import com.tle.web.institution.InstitutionSection;
import com.tle.web.institution.section.ProgressSection;
import com.tle.web.institution.section.TabsSection;
import com.tle.web.institution.tab.AdminTab;
import com.tle.web.institution.tab.DatabaseTab;
import com.tle.web.institution.tab.EmailsTab;
import com.tle.web.institution.tab.HealthTab;
import com.tle.web.institution.tab.ImportTab;
import com.tle.web.institution.tab.PasswordTab;
import com.tle.web.institution.tab.ServerMessageTab;
import com.tle.web.institution.tab.ServerTab;
import com.tle.web.institution.tab.ThreadDumpTab;
import com.tle.web.sections.equella.guice.SectionsModule;

public class InstitutionModule extends OptionalConfigModule
{

	@Override
	protected void configure()
	{
		install(new InstSectionModule());
		bindProp("versionserver.url", "https://version.equella.net/version");
	}

	public class InstSectionModule extends SectionsModule {
		@SuppressWarnings("nls")
		@Override
		protected void configure()
		{
			NodeProvider node = node(InstitutionSection.class);
			node.child(ProgressSection.class);
			node.child(TabsSection.class);
			node.child(AutoTestSetupSection.class);
			node.innerChild(AdminTab.class);
			node.innerChild(ImportTab.class);
			node.innerChild(DatabaseTab.class);
			node.innerChild(serverTab());
			node.innerChild(ThreadDumpTab.class);
			node.innerChild(HealthTab.class);
			bind(Object.class).annotatedWith(Names.named("/institutions")).toProvider(node);
		}

		private NodeProvider serverTab()
		{
			NodeProvider node = node(ServerTab.class);
			node.child(ServerMessageTab.class);
			node.child(EmailsTab.class);
			node.child(PasswordTab.class);
			return node;
		}
	}
}
