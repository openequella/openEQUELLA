package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class MoodleCronPage extends AbstractPage<MoodleCronPage>
{

	@FindBy(xpath = "id('notice')//input[@value='Purge all caches']")
	private WebElement purgeButton;

	public MoodleCronPage(PageContext context)
	{
		super(context, By.xpath("//pre[contains(text(),'Cron script completed correctly')]"), 60);
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getIntegUrl() + "admin/cron.php");
	}

}
