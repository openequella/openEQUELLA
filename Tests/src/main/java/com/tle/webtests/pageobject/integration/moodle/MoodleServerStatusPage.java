package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

public class MoodleServerStatusPage extends MoodleBasePage<MoodleServerStatusPage>
{
	@FindBy(xpath = "//input[@value='Continue']")
	private WebElement continueButton;

	public MoodleServerStatusPage(PageContext context)
	{
		super(context, By.id("serverstatus"));
	}

	public MoodlePluginCheckPage clickContinue()
	{
		continueButton.click();
		return new MoodlePluginCheckPage(context).get();
	}
}
