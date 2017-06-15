package com.tle.webtests.pageobject.settings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;

public class AddIPAddressPage extends AbstractPage<AddIPAddressPage>
{
	@FindBy(id = "{sectionId}_ipat")
	private WebElement ipAddressTextBox;
	@FindBy(id = "{sectionId}_ok")
	private WebElement ok;
	@FindBy(id = "{sectionId}_close")
	private WebElement cancel;

	public AddIPAddressPage(PageContext context)
	{
		super(context, By.xpath("//h3[text()='Add IP address']"));
	}

	public String getSectionId()
	{
		return "_addIpAddressDialog";
	}

	public AddIPAddressPage setIpAddress(String ipAddress)
	{
		ipAddressTextBox.clear();
		ipAddressTextBox.sendKeys(ipAddress);
		return get();
	}

	public String getInvalidAlertText()
	{
		return driver.switchTo().alert().getText();
	}

	public void dismissInvalidAlert()
	{
		driver.switchTo().alert().dismiss();
	}

	public String okWithError()
	{
		ok.click();
		String alertText = getInvalidAlertText();
		dismissInvalidAlert();
		return alertText;
	}

	public LoginSettingsPage ok(WaitingPageObject<LoginSettingsPage> returnTo)
	{
		ok.click();
		return returnTo.get();
	}

	public LoginSettingsPage cancel()
	{
		ExpectedCondition<Boolean> removalContition = ExpectedConditions2.stalenessOrNonPresenceOf(loadedElement);
		cancel.click();
		return ExpectWaiter.waiter(removalContition, new LoginSettingsPage(context)).get();
	}
}
