package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;

public class MyResourcesPortalSection extends AbstractPortalSection<MyResourcesPortalSection>
{
	public MyResourcesPortalSection(PageContext context, String title)
	{
		super(context, title);
	}

	public void clickLink(String title)
	{
		boxContent.findElement(By.xpath("div/a[text()=" + quoteXPath(title) + "]")).click();
	}
}
