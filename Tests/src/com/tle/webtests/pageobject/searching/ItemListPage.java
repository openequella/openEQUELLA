package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.tle.webtests.framework.PageContext;

public class ItemListPage extends AbstractItemList<ItemListPage, ItemSearchResult>
{
	public ItemListPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected ItemSearchResult createResult(SearchContext searchContext, By by)
	{
		return new ItemSearchResult(this, searchContext, by);
	}
}
