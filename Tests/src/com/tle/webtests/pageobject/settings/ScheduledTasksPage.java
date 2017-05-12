package com.tle.webtests.pageobject.settings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class ScheduledTasksPage extends AbstractPage<ScheduledTasksPage>
{
	@FindBy(linkText = "com.tle.core.workflow.daily.CheckReviewTask")
	WebElement checkReviewLink;

	@FindBy(linkText = "com.tle.core.workflow.daily.NotifyOfNewItemsTask")
	WebElement notifyNewTasksLink;

	@FindBy(linkText = "com.tle.core.payment.storefront.task.CheckUpdatedPurchasedItemsTask")
	WebElement checkUpdatedPurchasedItemsLink;

	@FindBy(linkText = "com.tle.core.payment.storefront.task.CheckCurrentOrdersTask")
	WebElement checkOrdersTaskLink;

	@FindBy(linkText = "com.tle.core.workflow.daily.CheckModerationTask")
	WebElement checkModerationLink;

	@FindBy(linkText = "Blackboard Connector Synchronisation")
	WebElement blackboardSyncLink;

	@FindBy(linkText = "Check-URLs")
	WebElement checkUrlsLink;

	@FindBy(linkText = "Run Harvester Profiles")
	WebElement runHarvesterProfilesLink;

	public ScheduledTasksPage(PageContext context)
	{
		super(context, By.xpath("//div[@id='header-inner']/div[text()='Task debug page']"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/scheduledtasksdebug.do");
	}

	public ScheduledTasksPage runCheckReview()
	{
		return clickAndUpdate(checkReviewLink);
	}

	public ScheduledTasksPage runNotifyNewTasks()
	{
		return clickAndUpdate(notifyNewTasksLink);
	}

	public ScheduledTasksPage runCheckModeration()
	{
		return clickAndUpdate(checkModerationLink);
	}

	public ScheduledTasksPage runCheckUrls()
	{
		return clickAndUpdate(checkUrlsLink);
	}

	public ScheduledTasksPage runCheckOrdersTask(boolean really)
	{
		return clickAndUpdate(checkOrdersTaskLink);
	}

	public ScheduledTasksPage runCheckUpdatedPurchasedItemsTask(boolean really)
	{
		return clickAndUpdate(checkUpdatedPurchasedItemsLink);
	}

	public ScheduledTasksPage runSyncEquellaContent()
	{
		return clickAndUpdate(blackboardSyncLink);
	}

	public ScheduledTasksPage runHarvesterProfiles()
	{
		return clickAndUpdate(runHarvesterProfilesLink);
	}
}
