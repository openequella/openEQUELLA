package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

public class MoodleUpgradingPage extends MoodleBasePage<MoodleUpgradingPage>
{
	@FindBy(xpath = "//input[@value='Continue']")
	private WebElement continueButton;

	public MoodleUpgradingPage(PageContext context)
	{
		super(context, By.xpath("//input[@value='Continue']"), 180);
	}

	public MoodleLoginPage clickContinue()
	{
		continueButton.click();
		return new MoodleLoginPage(context);
	}
}
