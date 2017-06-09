package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;

public class SearchPortalEditPage extends AbstractPortalEditPage<SearchPortalEditPage>
{
	public SearchPortalEditPage(PageContext context)
	{
		super(context);
	}

	@Override
	public String getType()
	{
		return "Quick search";
	}

	@Override
	public String getId()
	{
		return "sch";
	}
}
