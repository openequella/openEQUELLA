package com.tle.web.lti.consumers.module;

import com.google.inject.name.Names;
import com.tle.web.lti.consumers.section.LtiConsumersContributeSection;
import com.tle.web.lti.consumers.section.RootLtiConsumersSection;
import com.tle.web.lti.consumers.section.ShowLtiConsumersSection;
import com.tle.web.sections.equella.guice.SectionsModule;

public class LtiConsumersModule extends SectionsModule
{

	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("ltiConsumersTree")).toProvider(ltiConsumersTree());
	}

	private NodeProvider ltiConsumersTree()
	{
		NodeProvider node = node(RootLtiConsumersSection.class);
		node.child(ShowLtiConsumersSection.class);
		node.innerChild(LtiConsumersContributeSection.class);
		return node;
	}

}
