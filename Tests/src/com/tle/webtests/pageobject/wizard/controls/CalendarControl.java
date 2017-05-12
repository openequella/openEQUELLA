package com.tle.webtests.pageobject.wizard.controls;

import java.util.Date;
import java.util.TimeZone;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.Calendar;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class CalendarControl extends AbstractWizardControl<CalendarControl>
{
	@FindBy(id = "{wizid}_clearLink")
	private WebElement clearLink;
	@FindBy(id = "{wizid}_calendar-control")
	private WebElement mainDiv;
	@FindBy(id = "{wizid}_clear")
	private WebElement clearDiv;
	private Calendar cal1;
	private Calendar cal2;

	public CalendarControl(AbstractWizardControlPage<?> page, int ctrlnum)
	{
		super(page.getContext(), ctrlnum, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return mainDiv;
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
		WaitingPageObject<CalendarControl> ajaxUpdate = ajaxUpdate(mainDiv);
		clearLink.click();
		ajaxUpdate.get();
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
		cal1.get().setDate(cal, ajaxUpdate(clearDiv));
	}

	public void setDateRange(java.util.Calendar start, java.util.Calendar end)
	{
		cal1.get().setDate(start, ajaxUpdate(clearDiv));
		cal2.get().setDate(end, ajaxUpdate(clearDiv));
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
		cal2.get().setDate(endDate, ajaxUpdate(clearDiv));
	}

}
