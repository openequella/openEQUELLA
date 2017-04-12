package com.tle.web.contribute.guice;

import com.tle.web.contribute.ContributeSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class ContributeModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		NodeProvider node = node(ContributeSection.class);
		addChildren(node);
		bindNamed(getTree(), node);
	}

	protected String getTree()
	{
		return "/access/contribute";
	}

	protected void addChildren(NodeProvider node)
	{
		// To override
	}
}
