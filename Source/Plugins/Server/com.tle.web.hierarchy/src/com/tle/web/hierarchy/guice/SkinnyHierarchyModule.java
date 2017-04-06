package com.tle.web.hierarchy.guice;

import com.tle.web.hierarchy.section.HierarchyIntegrationShareSearchQuerySection;
import com.tle.web.sections.Section;


@SuppressWarnings("nls")
public class SkinnyHierarchyModule extends HierarchySearchModule
{
	@Override
	protected String getTreeName()
	{
		return "/access/skinny/hierarchy";
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		// No actions
	}

	@Override
	protected Class<? extends Section> getShareSection()
	{
		return HierarchyIntegrationShareSearchQuerySection.class;
	}
}
