package com.tle.webtests.pageobject.viewitem;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

public class AdminTabPage extends ItemPage<AdminTabPage>
{
	@FindBy(xpath = "//h3[text()='Actions']")
	private WebElement actionsElem;

	public AdminTabPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return actionsElem;
	}
}
