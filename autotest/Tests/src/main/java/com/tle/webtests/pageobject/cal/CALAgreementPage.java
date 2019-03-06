package com.tle.webtests.pageobject.cal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;

public class CALAgreementPage extends AbstractPage<CALAgreementPage>
{
	@FindBy(xpath = "//button[normalize-space(text())='Accept']")
	private WebElement acceptButton;
	@FindBy(xpath = "//button[normalize-space(text())='Reject']")
	private WebElement rejectButton;
	@FindBy(id = "copyright-agreement")
	private WebElement agreementText;

	public CALAgreementPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return agreementText;
	}

	public void reject()
	{
		rejectButton.click();
	}

	public String getAgreementText()
	{
		return agreementText.getText();
	}

	public <T extends PageObject> T accept(WaitingPageObject<T> acceptedPage)
	{
		acceptButton.click();
		return acceptedPage.get();
	}

}
