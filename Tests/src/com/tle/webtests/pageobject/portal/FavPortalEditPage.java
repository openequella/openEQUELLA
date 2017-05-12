package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;

public class FavPortalEditPage extends AbstractPortalEditPage<FavPortalEditPage>
{
	public FavPortalEditPage(PageContext context)
	{
		super(context);
	}

	@Override
	public String getType()
	{
		return "Favourites";
	}

	@Override
	public String getId()
	{
		return "fpe";
	}
}
