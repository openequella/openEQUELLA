package com.tle.web.institution.guice;

import com.google.inject.name.Names;
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

public class InstitutionModule extends SectionsModule
{
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
