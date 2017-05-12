package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.myresources.AbstractAuthorWebPage;

public class HtmlPortalEditPage extends AbstractPortalEditPage<HtmlPortalEditPage>
{
	public HtmlPortalEditPage(PageContext context)
	{
		super(context, By.id(AbstractAuthorWebPage.IFRAME_ID_PREFIX));
	}

	@Override
	public String getType()
	{
		return "Formatted text";
	}

	public void setText(String text)
	{
		driver.switchTo().frame(AbstractAuthorWebPage.IFRAME_ID_PREFIX);
		driver.switchTo().activeElement();
		((JavascriptExecutor) driver).executeScript("document.body.innerHTML = " + quoteXPath(text));
		driver.switchTo().defaultContent();
	}

	@Override
	public String getId()
	{
		return "hpe";
	}
}
