package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.SelectGroupDialog;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class SelectGroupControl extends AbstractWizardControl<SelectGroupControl>
{
	private WebElement getSelectGroupButton()
	{
		return byWizId("_addLink");
	}

	private WebElement getRootElem()
	{
		return byWizId("groupselector");
	}


	public SelectGroupControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getSelectGroupButton();
	}

	private String xpathForGroupname(String groupname)
	{
		return ".//tr/td[@class='name' and text()=" + quoteXPath(groupname) + "]";
	}

	public SelectGroupDialog openDialog()
	{
		getSelectGroupButton().click();
		return new SelectGroupDialog(context, page.subComponentId(ctrlnum, "s")).get();
	}

	public AbstractWizardControlPage<?> queryAndSelect(String query, String groupname)
	{
		openDialog().search(query).selectAndFinish(groupname, selectedWaiter(groupname));
		return page;
	}

	public void removeGroup(String groupname)
	{
		WaitingPageObject<SelectGroupControl> waiter = removedWaiter(groupname);
		getRootElem().findElement(By.xpath(xpathForGroupname(groupname) + "/../td[@class='actions']/a[@class='unselect']"))
			.click();
		acceptConfirmation();
		waiter.get();
	}

	public WaitingPageObject<SelectGroupControl> selectedWaiter(String newlySelected)
	{
		return ExpectWaiter.waiter(
			ExpectedConditions2.visibilityOfElementLocated(getRootElem(), By.xpath(xpathForGroupname(newlySelected))), this);
	}

	public WaitingPageObject<SelectGroupControl> removedWaiter(String removed)
	{
		return ExpectWaiter.waiter(
			ExpectedConditions2.invisibilityOfElementLocated(getRootElem(), By.xpath(xpathForGroupname(removed))), this);
	}
}
