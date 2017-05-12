package com.tle.webtests.pageobject.cal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class CALOverridePage extends AbstractPage<CALOverridePage>
{
	@FindBy(id = "reason")
	private WebElement reasonTextField;
	@FindBy(id = "calpo_continueButton")
	private WebElement continueButton;
	
	private final CALSummaryPage returnTo;
	
	public CALOverridePage(PageContext context, CALSummaryPage returnTo)
	{
		super(context, By.id("reason"));
		this.returnTo = returnTo;
	}
	
	public void setReason(String reason)
	{
		reasonTextField.sendKeys(reason);
	}

	public CALSummaryPage clickContinue()
	{
		continueButton.click();
		return returnTo.get();
	}
}
