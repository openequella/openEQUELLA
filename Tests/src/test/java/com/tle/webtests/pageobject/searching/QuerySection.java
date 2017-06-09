package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

public class QuerySection extends AbstractQuerySection<QuerySection>
{
	@FindBy(id = "searchform-search")
	private WebElement searchButton;

	public QuerySection(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement getSearchButton()
	{
		return searchButton;
	}

}
