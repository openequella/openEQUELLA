package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;

public class RecentContributionsSection extends AbstractPortalSection<RecentContributionsSection>
{

	public RecentContributionsSection(PageContext context, String title)
	{
		super(context, title);
	}

	public boolean recentContributionExists(String itemName)
	{
		return isPresent(
			boxContent,
			By.xpath(".//div[normalize-space(@class)='recent-items']//a[normalize-space(text())="
				+ quoteXPath(itemName) + "]"));
	}

	public boolean descriptionExists(String description)
	{
		return isPresent(
			boxContent,
			By.xpath(".//div[normalize-space(@class)='recent-items']//p[normalize-space(text())="
				+ quoteXPath(description) + "]"));
	}

}
