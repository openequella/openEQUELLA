package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

public class FilterByKeywordPage extends AbstractQuerySection<FilterByKeywordPage>
{
	@FindBy(id = "{buttonId}")
	private WebElement searchButton;
	private final String buttonId;

	public FilterByKeywordPage(PageContext context)
	{
		this(context, "fbakw_s");
	}

	public FilterByKeywordPage(PageContext context, String buttonId)
	{
		super(context);
		this.buttonId = buttonId;
	}

	@Override
	protected WebElement getSearchButton()
	{
		return searchButton;
	}

	public String getButtonId()
	{
		return buttonId;
	}
}
