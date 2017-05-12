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
	@FindBy(id = "{wizid}_addLink")
	private WebElement selectUserButton;
	@FindBy(id = "{wizid}userselector")
	private WebElement rootElem;

	public SelectUserControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return selectUserButton;
	}

	private String xpathForUsername(String username)
	{
		return ".//tr/td[@class='name']/span[@title=" + quoteXPath(username) + "]";
	}

	public SelectUserDialog openDialog()
	{
		selectUserButton.click();
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
		rootElem.findElement(By.xpath(xpathForUsername(username) + "/../../td[@class='actions']/a[@class='unselect']"))
			.click();
		acceptConfirmation();
		waiter.get();
	}

	public WaitingPageObject<SelectUserControl> selectedWaiter(String newlySelected)
	{
		return ExpectWaiter.waiter(
			ExpectedConditions2.visibilityOfElementLocated(rootElem, By.xpath(xpathForUsername(newlySelected))), this);
	}

	public WaitingPageObject<SelectUserControl> removedWaiter(String removed)
	{
		return ExpectWaiter.waiter(
			ExpectedConditions2.invisibilityOfElementLocated(rootElem, By.xpath(xpathForUsername(removed))), this);
	}
}
