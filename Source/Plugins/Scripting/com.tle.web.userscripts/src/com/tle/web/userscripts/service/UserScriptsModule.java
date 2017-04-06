package com.tle.web.userscripts.service;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.userscripts.section.RootUserScriptsSection;
import com.tle.web.userscripts.section.ShowUserScriptsSection;
import com.tle.web.userscripts.section.UserScriptContributeSection;

public class UserScriptsModule extends SectionsModule
{

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("userScriptsTree")).toProvider(userScriptsTree());

	}

	private NodeProvider userScriptsTree()
	{
		NodeProvider node = node(RootUserScriptsSection.class);
		node.innerChild(UserScriptContributeSection.class);
		node.child(ShowUserScriptsSection.class);
		return node;
	}


}
