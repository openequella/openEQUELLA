package com.tle.webtests.pageobject.cal;

import com.tle.webtests.pageobject.generic.component.Select2Select;
import com.tle.webtests.pageobject.generic.component.SelectCourseDialog;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.BulkActionDialog;
import com.tle.webtests.pageobject.searching.BulkResultsPage;

public class CALRolloverDialog extends AbstractPage<CALRolloverDialog>
{
	@FindBy(id = "bro_c")
	private WebElement courseDropDown;
	private final BulkActionDialog dialog;
	@FindBy(id = "bro_rd")
	private WebElement rolloverDatesCheckbox;

	public CALRolloverDialog(BulkActionDialog dialog)
	{
		super(dialog.getContext());
		mustBeVisible = false;
		this.dialog = dialog;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return courseDropDown;
	}

	public CALRolloverDialog selectCourse(String course)
	{
		WaitingPageObject<BulkActionDialog> updater = dialog.updateWaiter();
		SelectCourseDialog selectCourseDialog = new SelectCourseDialog(context, "bro_c");
		selectCourseDialog.searchSelectAndFinish(course, updater);
		return get();
	}

	public <T extends PageObject> boolean execute(WaitingPageObject<T> targetPage)
	{
		BulkResultsPage resultsPage = dialog.execute();
		resultsPage.waitForAll();
		boolean noErrors = resultsPage.noErrors();
		resultsPage.close(targetPage);
		return noErrors;
	}

	public void setRolloverDates(boolean rolloverDates)
	{
		if( rolloverDatesCheckbox.isSelected() ^ rolloverDates )
		{
			rolloverDatesCheckbox.click();
		}
	}
}
