package com.tle.webtests.pageobject.portal;

import java.text.MessageFormat;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;

public class TaskStatisticsPortalSection extends AbstractPortalSection<TaskStatisticsPortalSection>
{
	public TaskStatisticsPortalSection(PageContext context, String title)
	{
		super(context, title);
	}

	public boolean isTrendSelected(String trend)
	{
		return isPresent(By.xpath(MessageFormat.format("//div[@id=\"ptsprtrendselector\"]//strong[text()={0}]",
			quoteXPath(trend))));
	}
}