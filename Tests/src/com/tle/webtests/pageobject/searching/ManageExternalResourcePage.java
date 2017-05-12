package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

public class ManageExternalResourcePage
	extends
		AbstractBulkResultsPage<ManageExternalResourcePage, ItemListPage, ItemSearchResult>
{
	@FindBy(id = "searchform-in")
	private WebElement connectorDropDown;
	private EquellaSelect connectorSelect;

	@FindBy(id = "course")
	private WebElement courseDropDown;

	@FindBy(id = "fba_includeArchived")
	private WebElement showArchived;

	@FindBy(id = "reverse")
	private WebElement reverseBox;

	public ManageExternalResourcePage(PageContext context)
	{
		super(context);
		setMustBeVisible(false);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return connectorDropDown;
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/manageconnectors.do");
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		connectorSelect = new EquellaSelect(context, connectorDropDown);
	}

	public ManageExternalResourcePage selectConnector(PrefixedName connector)
	{
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		connectorSelect.selectByVisibleText(connector.toString());
		return waitForResultsReload(waiter);
	}

	public boolean singleConnector()
	{
		return !isPresent(By.id("searchform-in"));
	}

	public ManageExternalResourcePage showArchived(boolean on)
	{
		openFilters();
		if( on == Check.isEmpty(showArchived.getAttribute("checked")) )
		{
			WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
			showArchived.click();
			return waitForResultsReload(waiter);
		}
		return this;
	}

	public ManageExternalResourcePage setSortRevese(boolean on)
	{
		openSort();
		if( on == Check.isEmpty(reverseBox.getAttribute("checked")) )
		{
			WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
			reverseBox.click();
			return waitForResultsReload(waiter);
		}
		return this;
	}

	public ManageExternalResourcePage selectCourse(String course)
	{
		openFilters();
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		EquellaSelect courseSelect = new EquellaSelect(context, courseDropDown);
		if( Check.isEmpty(course) )
		{
			courseSelect.selectByIndex(0);
		}
		else
		{
			courseSelect.selectByVisibleText(course);
		}

		return waitForResultsReload(waiter);
	}

	@Override
	public ItemListPage resultsPageObject()
	{
		return new ItemListPage(context);
	}

	public ExternalBulkSection externalBulk()
	{
		return new ExternalBulkSection(bulk());
	}

}
