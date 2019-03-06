package com.tle.webtests.pageobject.cal;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.component.SelectCourseDialog;
import com.tle.webtests.pageobject.searching.AbstractBulkResultsPage;

public class ManageActivationsPage
	extends
		AbstractBulkResultsPage<ManageActivationsPage, ActivationListPage, ActivationSearchResult>
{
	@FindBy(xpath = "//div[@id='content-body' and contains(@class, 'activations-layout')]")
	private WebElement mainElem;
	@FindBy(id = "fbas_as")
	private WebElement statusElem;
	@FindBy(id = "rf_resetButton")
	private WebElement resetFilters;
	@FindBy(id = "fbc_sc_opener")
	private WebElement courseFilterOpener;

	private EquellaSelect statusFilter;

	public ManageActivationsPage(PageContext context)
	{
		super(context);

	}

	@Override
	protected WebElement findLoadedElement()
	{
		return mainElem;
	}

	@Override
	public void checkLoaded() throws NotFoundException
	{
		super.checkLoaded();
		openFilters();
		statusFilter = new EquellaSelect(context, statusElem).get();
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/activations.do");
	}

	public CALBulkSection bulkcal()
	{
		return new CALBulkSection(bulk());
	}

	@Override
	public ActivationListPage resultsPageObject()
	{
		return new ActivationListPage(context);
	}

	public ManageActivationsPage filterByStatus(String status)
	{
		openFilters();
		WaitingPageObject<ActivationListPage> waiter = resultsPageObject.getUpdateWaiter();
		statusFilter.selectByVisibleText(status);
		return waitForResultsReload(waiter);
	}

	public ManageActivationsPage filterByCourse(String course)
	{
		openFilters();
		WaitingPageObject<ActivationListPage> waiter = resultsPageObject.getUpdateWaiter();
		SelectCourseDialog scd = new SelectCourseDialog(context, "course").get();
		scd.searchSelectAndFinish(course, this);
		return waitForResultsReload(waiter);
	}

	public ManageActivationsPage resetFilters()
	{
		try
		{
			WaitingPageObject<ActivationListPage> waiter = resultsPageObject.getUpdateWaiter();
			WebElement resetButton = context.getDriver().findElement(By.id("rf_resetButton"));
			resetButton.click();
			return waitForResultsReload(waiter);
		}
		catch( NoSuchElementException noseeum )
		{
		}
		return this;
	}

}
