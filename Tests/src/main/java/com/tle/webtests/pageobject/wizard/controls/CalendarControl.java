package com.tle.webtests.pageobject.wizard.controls;

import java.util.Date;
import java.util.TimeZone;

import com.tle.webtests.pageobject.ExpectWaiter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.Calendar;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class CalendarControl extends AbstractWizardControl<CalendarControl>
{
	private WebElement getClearLink()
	{
		return byWizId("_clearLink");
	}

	private WebElement getMainDiv()
	{
		return byWizId("_calendar-control");
	}

	private By getClearDiv()
	{
		return wizIdBy("_clear");
	}

	private Calendar cal1;
	private Calendar cal2;

	public CalendarControl(AbstractWizardControlPage<?> page, int ctrlnum)
	{
		super(page.getContext(), ctrlnum, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getMainDiv();
	}

	@Override
	protected void checkLoadedElement()
	{
		super.checkLoadedElement();
		cal1 = new Calendar(context, getWizid() + "_date1");
		cal2 = new Calendar(context, getWizid() + "_date2");
	}

	public void clearDate()
	{
		WaitingPageObject<CalendarControl> ajaxUpdate = ajaxUpdate(getMainDiv());
		getClearLink().click();
		ajaxUpdate.get();
	}

	public void clearDateRemove()
	{
		getClearLink().click();
	}

	// As far as I can tell, this is only used with 'conceptual' dates, which is
	// good.
	public void setDate(Date date)
	{
		// Ummmm, this has to be UTC, not the timezone of the machine running
		// the auto test suite!!!
		java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		cal.setTime(date);
		setDate(cal);
	}

	public void setDate(java.util.Calendar cal)
	{
		cal1.get().setDate(cal, ajaxUpdate(getClearDiv()));
	}

	public void setDateWithReload(java.util.Calendar cal)
	{
		cal1.get().setDate(cal, ExpectWaiter.waiter(updatedCondition(), this));
	}
	public void setDateRange(java.util.Calendar start, java.util.Calendar end)
	{
		cal1.get().setDate(start, ajaxUpdate(getClearDiv()));
		cal2.get().setDate(end, ajaxUpdate(getClearDiv()));
	}

	public void setDateRange(Date start, Date end)
	{
		java.util.Calendar startCal = java.util.Calendar.getInstance();
		startCal.setTime(start);
		java.util.Calendar endCal = java.util.Calendar.getInstance();
		endCal.setTime(end);
		setDateRange(startCal, endCal);
	}

	public boolean isRangeDisabled()
	{
		return cal1.get().isDisabled() && cal2.get().isDisabled();
	}

	public void setEndDate(Date endDate)
	{
		cal2.get().setDate(endDate, ajaxUpdate(getClearDiv()));
	}

}
