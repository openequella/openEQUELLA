package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class ItemSearchResult extends AbstractItemSearchResult<ItemSearchResult>
{

	public ItemSearchResult(AbstractResultList<?, ?> page, SearchContext relativeTo, By by)
	{
		super(page, relativeTo, by);
	}

}
