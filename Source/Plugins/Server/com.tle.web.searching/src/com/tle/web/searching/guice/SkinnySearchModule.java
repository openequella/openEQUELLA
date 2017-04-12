package com.tle.web.searching.guice;

import com.tle.web.searching.section.IntegrationShareSearchQuerySection;


@SuppressWarnings("nls")
public class SkinnySearchModule extends StandardSearchModule
{
	@Override
	protected NodeProvider getShareSection()
	{
		return node(IntegrationShareSearchQuerySection.class);
	}

	@Override
	protected NodeProvider getActionsNode()
	{
		return null;
	}

	@Override
	protected String getTreeName()
	{
		return "/access/skinny/search";
	}
}
