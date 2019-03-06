package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.Calendar;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

public class FilterByDateSectionPage extends AbstractPage<FilterByDateSectionPage>
{
	@FindBy(id = "fbdr_clearButton")
	private WebElement clearLink;

	private EquellaSelect dateFilterList;
	private Calendar datePrimary;
	private Calendar dateSecondary;

	private AbstractResultList<?, ?> resultsPageObject;

	public FilterByDateSectionPage(PageContext context, AbstractResultList<?, ?> resultsPageObject)
	{
		super(context, By.id("dr"));
		setMustBeVisible(false);
		this.resultsPageObject = resultsPageObject;
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		dateFilterList = new EquellaSelect(context, driver.findElement(loadedBy));
		datePrimary = new Calendar(context, "dp");
		dateSecondary = new Calendar(context, "ds");
	}

	public void setDateFilter(String option, java.util.Calendar[] dates)
	{
		String selectedValue = dateFilterList.getSelectedValue();

		if( !option.equals(selectedValue) )
		{
			WaitingPageObject<?> updateWaiter = resultsPageObject.getUpdateWaiter();
			dateFilterList.selectByValue(option);
			updateWaiter.get();

		}
		WaitingPageObject<?> updateWaiter = resultsPageObject.getUpdateWaiter();
		datePrimary.get().setDate(dates[0], updateWaiter);
		if( option.equals("BETWEEN") && dates[1] != null )
		{
			updateWaiter = resultsPageObject.getUpdateWaiter();
			dateSecondary.get().setDate(dates[1], updateWaiter);
		}
	}

	public Calendar getStartDate()
	{
		return datePrimary;
	}

	public Calendar getEndDate()
	{
		return dateSecondary;
	}

	// public String[] getDisplayedDates()
	// {
	// final String d1 = datePrimary.get().getTextValue();
	// final String d2;
	// if( dateSecondary.isVisible() )
	// {
	// d2 = dateSecondary.get().getTextValue();
	// }
	// else
	// {
	// d2 = null;
	// }
	// return new String[]{d1, d2};
	// }

	public void clearDateFilter()
	{
		if( isPresent(clearLink) )
		{
			clearLink.click();
		}
	}
}
