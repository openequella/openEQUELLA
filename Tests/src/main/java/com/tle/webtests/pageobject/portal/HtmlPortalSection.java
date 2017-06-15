package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;

public class HtmlPortalSection extends AbstractPortalSection<HtmlPortalSection>
{
	public HtmlPortalSection(PageContext context, String title)
	{
		super(context, title);
	}

	public String portalText()
	{
		return boxContent.findElement(By.xpath("div/p")).getText();
	}

}
