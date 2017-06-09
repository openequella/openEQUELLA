package com.tle.webtests.pageobject.payment.storefront;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.Calendar;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.ItemListPage;

// copied from FilterByDateSectionPage, just changed id's
public class FilterBySubscriptionEndDatePage extends AbstractPage<FilterBySubscriptionEndDatePage>
{
	@FindBy(id = "fbse_clearButton")
	private WebElement clearLink;

	private EquellaSelect dateFilterList;
	private Calendar datePrimary;
	private Calendar dateSecondary;
	private ItemListPage resultsPageObject;

	public FilterBySubscriptionEndDatePage(PageContext context, ItemListPage resultsPageObject)
	{
		super(context, By.id("sdr"));
		setMustBeVisible(false);
		this.resultsPageObject = resultsPageObject;
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		dateFilterList = new EquellaSelect(context, driver.findElement(loadedBy));
		datePrimary = new Calendar(context, "sdp");
		dateSecondary = new Calendar(context, "sds");
	}

	public ItemListPage setDateFilter(String option, java.util.Calendar[] dates)
	{
		String selectedValue = dateFilterList.getSelectedValue();

		if( !option.equals(selectedValue) )
		{
			WaitingPageObject<ItemListPage> updateWaiter = resultsPageObject.getUpdateWaiter();
			dateFilterList.selectByValue(option);
			updateWaiter.get();

		}
		ItemListPage itemListPage = datePrimary.get().setDate(dates[0], resultsPageObject.getUpdateWaiter());
		if( option.equals("BETWEEN") && dates[1] != null )
		{
			itemListPage = dateSecondary.get().setDate(dates[1], resultsPageObject.getUpdateWaiter());
		}
		return itemListPage;
	}

	public void clearDateFilter()
	{
		if( isPresent(clearLink) )
		{
			clearLink.click();
		}
	}
}
