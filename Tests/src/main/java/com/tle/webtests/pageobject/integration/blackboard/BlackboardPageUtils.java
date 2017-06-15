package com.tle.webtests.pageobject.integration.blackboard;

import org.openqa.selenium.By;

import com.tle.webtests.pageobject.AbstractPage;

public class BlackboardPageUtils
{

	public static By pageTitleBy(String title)
	{
		return By.xpath("id('pageTitleBar')/h1/span[normalize-space(text())=" + AbstractPage.quoteXPath(title) + "]");
	}
}
