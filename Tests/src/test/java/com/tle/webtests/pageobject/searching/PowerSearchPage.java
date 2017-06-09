package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;

import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class PowerSearchPage extends AbstractWizardControlPage<PowerSearchPage>
{
	private SearchPage searchPage;

	public PowerSearchPage(SearchPage searchPage)
	{
		super(searchPage.getContext(), By.id("wizard-controls"), 0);
		this.searchPage = searchPage;
	}

	@Override
	public String getControlId(int ctrlNum)
	{
		return "p" + pageNum + "c" + ctrlNum;
	}

	public SearchPage search()
	{
		WaitingPageObject<ItemListPage> waiter = searchPage.getResultsUpdateWaiter();
		driver.findElement(By.id("searchform-advanced-search")).click();
		return searchPage.waitForResultsReload(waiter);
	}

	public String getRawHtmlText(int ctrlNum)
	{
		return driver.findElement(By.id(getControlId(ctrlNum))).getText();
	}

}
