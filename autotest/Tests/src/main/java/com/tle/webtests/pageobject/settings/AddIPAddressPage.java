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
	private WebElement getIpAddressTextBox()
	{
		return findBySectionId("_ipat");
	}

	private WebElement findBySectionId(String postfix)
	{
		return findWithId(getSectionId(), postfix);
	}

	private WebElement getOk()
	{
		return findBySectionId("_ok");
	}
	private WebElement getCancel()
	{
		return findBySectionId("_close");
	}

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
		getIpAddressTextBox().clear();
		getIpAddressTextBox().sendKeys(ipAddress);
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
		getOk().click();
		String alertText = getInvalidAlertText();
		dismissInvalidAlert();
		return alertText;
	}

	public LoginSettingsPage ok(WaitingPageObject<LoginSettingsPage> returnTo)
	{
		getOk().click();
		return returnTo.get();
	}

	public LoginSettingsPage cancel()
	{
		ExpectedCondition<Boolean> removalContition = ExpectedConditions2.stalenessOrNonPresenceOf(loadedElement);
		getCancel().click();
		return ExpectWaiter.waiter(removalContition, new LoginSettingsPage(context)).get();
	}
}
