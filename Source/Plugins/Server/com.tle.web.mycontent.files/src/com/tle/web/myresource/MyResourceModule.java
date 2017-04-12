package com.tle.web.myresource;

import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.guice.SectionsModule;

public class MyResourceModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(SectionTree.class).toProvider(tree(node(MyResourceContributeSection.class)));
	}

}
