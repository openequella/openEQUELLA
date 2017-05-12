package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class EmailSelectorControl extends AbstractWizardControl<EmailSelectorControl>
{
	@FindBy(id = "{wizid}emailControl")
	private WebElement rootElem;
	@FindBy(id = "{wizid}_e")
	private WebElement emailField;
	@FindBy(id = "{wizid}_s_opener")
	private WebElement dialogOpener;
	@FindBy(id = "{wizid}_a")
	private WebElement addButton;
	@FindBy(xpath = "id('{wizid}emailControl')/div[@class='noemailwarning' and text() = 'Invalid email address']")
	private WebElement emailError;

	public EmailSelectorControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return rootElem;
	}

	public void addEmail(String email)
	{
		addEmail(email, false);
	}

	public void addEmail(String email, boolean invalid)
	{
		WaitingPageObject<EmailSelectorControl> waiter;
		if( !invalid )
		{
			waiter = selectedWaiter(email);
		}
		else
		{
			waiter = ExpectWaiter.waiter(ExpectedConditions2.presenceOfElement(emailError), this);
		}
		emailField.clear();
		emailField.sendKeys(email);
		addButton.click();
		waiter.get();
	}

	private String xpathForEmail(String email)
	{
		return "//tr[td[contains(@class,'name') and normalize-space(text()) = " + quoteXPath(email) + "]]";
	}

	public SelectUserDialog openDialog()
	{
		dialogOpener.click();
		return new SelectUserDialog(context, page.subComponentId(ctrlnum, "s")).get();
	}

	public AbstractWizardControlPage<?> queryAndSelect(String query, String username)
	{
		openDialog().search(query).selectAndFinish(username, selectedWaiter(username));
		return page;
	}

	public boolean isAddAvailable()
	{
		try
		{
			return addButton.isDisplayed();
		}
		catch( NotFoundException nfe )
		{
			return false;
		}
	}

	public EmailSelectorControl removeEmail(String email)
	{
		WaitingPageObject<EmailSelectorControl> waiter = removedWaiter(email);
		String xpathExpression = xpathForEmail(email) + "/td/a";
		rootElem.findElement(By.xpath(xpathExpression)).click();
		acceptConfirmation();
		return waiter.get();
	}

	public boolean isInvalidEmail()
	{
		return isPresent(emailError);
	}

	public WaitingPageObject<EmailSelectorControl> selectedWaiter(String newlySelected)
	{
		return ExpectWaiter.waiter(
			ExpectedConditions2.visibilityOfElementLocated(rootElem, By.xpath(xpathForEmail(newlySelected))), this);
	}

	public WaitingPageObject<EmailSelectorControl> removedWaiter(String removed)
	{
		return ExpectWaiter.waiter(
			ExpectedConditions2.invisibilityOfElementLocated(rootElem, By.xpath(xpathForEmail(removed))), this);
	}

}
