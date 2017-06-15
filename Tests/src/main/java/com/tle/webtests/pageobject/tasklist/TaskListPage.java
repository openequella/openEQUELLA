package com.tle.webtests.pageobject.tasklist;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.AbstractQueryableSearchPage;
import com.tle.webtests.pageobject.searching.ModerateListSearchResults;
import com.tle.webtests.pageobject.searching.ModerationSearchResult;

public class TaskListPage
	extends
		AbstractQueryableSearchPage<TaskListPage, ModerateListSearchResults, ModerationSearchResult>
{
	@FindBy(xpath = "id('header-inner')/div[text()='My tasks']")
	private WebElement mainElem;
	@FindBy(id = "unan")
	private WebElement mustMod;
	@FindBy(id = "asgn")
	private WebElement assignmentFilter;
	@FindBy(id = "rf_resetButton")
	private WebElement clearFilters;

	public TaskListPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return mainElem;
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/tasklist.do");
	}

	@Override
	public ModerateListSearchResults resultsPageObject()
	{
		return new ModerateListSearchResults(context);
	}

	public int getNumberOfResults()
	{
		return results().getTotalAvailable();
	}

	public TaskListPage setMustMod(boolean mustMod)
	{
		WaitingPageObject<ModerateListSearchResults> waiter = resultsPageObject.getUpdateWaiter();

		if( this.mustMod.isSelected() != mustMod )
		{
			this.mustMod.click();
		}
		return waitForResultsReload(waiter);
	}

	/**
	 * @param assignment: ALL, ME, OTHER, NOONE
	 * @return
	 */
	public TaskListPage setAssignmentFilter(String assignment)
	{
		openFilters();
		WaitingPageObject<ModerateListSearchResults> waiter = resultsPageObject.getUpdateWaiter();

		new EquellaSelect(context, assignmentFilter).selectByValue(assignment);
		return waitForResultsReload(waiter);
	}

	public TaskListPage clearFilters()
	{
		WaitingPageObject<ModerateListSearchResults> waiter = resultsPageObject.getUpdateWaiter();
		clearFilters.click();
		return waitForResultsReload(waiter);
	}
}
