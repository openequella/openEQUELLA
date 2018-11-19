package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class SelectUserControl extends AbstractWizardControl<SelectUserControl>
{
	private WebElement getSelectUserButton()
	{
		return byWizId("_addLink");
	}

	private WebElement getRootElem()
	{
		return byWizId("userselector");
	}


	public SelectUserControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getSelectUserButton();
	}

	private String xpathForUsername(String username)
	{
		return ".//tr/td[@class='name']/span[@title=" + quoteXPath(username) + "]";
	}

	public SelectUserDialog openDialog()
	{
		getSelectUserButton().click();
		return new SelectUserDialog(context, page.subComponentId(ctrlnum, "s")).get();
	}

	public AbstractWizardControlPage<?> queryAndSelect(String query, String username)
	{
		openDialog().search(query).selectAndFinish(username, selectedWaiter(username));
		return page;
	}

	public void removeUser(String username)
	{
		WaitingPageObject<SelectUserControl> waiter = removedWaiter(username);
		getRootElem().findElement(By.xpath(xpathForUsername(username) + "/../../td[@class='actions']/a[@class='unselect']"))
			.click();
		acceptConfirmation();
		waiter.get();
	}

	public WaitingPageObject<SelectUserControl> selectedWaiter(String newlySelected)
	{
		return ExpectWaiter.waiter(
			ExpectedConditions2.visibilityOfElementLocated(getRootElem(), By.xpath(xpathForUsername(newlySelected))), this);
	}

	public WaitingPageObject<SelectUserControl> removedWaiter(String removed)
	{
		return ExpectWaiter.waiter(
			ExpectedConditions2.invisibilityOfElementLocated(getRootElem(), By.xpath(xpathForUsername(removed))), this);
	}
}
