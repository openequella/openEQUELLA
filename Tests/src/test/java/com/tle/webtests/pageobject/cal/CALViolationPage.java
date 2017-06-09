package com.tle.webtests.pageobject.cal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;

public class CALViolationPage extends AbstractPage<CALViolationPage>
{
	@FindBy(id = "cancelButton")
	private WebElement cancelButton;

	public CALViolationPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return cancelButton;
	}

	public <T extends PageObject> T okViolation(WaitingPageObject<T> returnTo)
	{
		cancelButton.click();
		return returnTo.get();
	}

	@Override
	protected void isError()
	{
		// Probably isn't
		// FIXME: need to be more specific
	}
}
