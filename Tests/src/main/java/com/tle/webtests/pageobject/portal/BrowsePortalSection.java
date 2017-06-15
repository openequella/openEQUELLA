package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;

public class BrowsePortalSection extends AbstractPortalSection<BrowsePortalSection>
{
	public BrowsePortalSection(PageContext context, String title)
	{
		super(context, title);
	}

	public boolean topicExists(String topic)
	{
		return isPresent(boxContent, By.xpath(".//ul[@class='topics']/li/a[starts-with(., " + quoteXPath(topic) + ")]"));
	}
}
