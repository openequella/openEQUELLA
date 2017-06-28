package com.tle.webtests.pageobject.cal;

import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.component.Calendar;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.component.SelectCourseDialog;

public class CALActivatePage<T extends AbstractPage<T>> extends AbstractPage<CALActivatePage<T>>
{
	@FindBy(id = "cala_activateButton")
	private WebElement activateButton;
	@FindBy(id = "cala_cancelButton")
	private WebElement cancelButton;
	@FindBy(id = "cala_selectCourse")
	private WebElement selectCourseButton;
	@FindBy(id = "cala_c")
	private WebElement citationElem;

	private Calendar fromDate;
	private Calendar untilDate;
	private final T from;
	private EquellaSelect citation;

	public CALActivatePage(PageContext context, T from)
	{
		super(context, By.id("cala_activateButton"));
		this.from = from;
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		fromDate = new Calendar(context, "cala_fd").get();
		untilDate = new Calendar(context, "cala_ud").get();
		citation = new EquellaSelect(context, citationElem).get();
	}

	public CALViolationPage activateViolation()
	{
		activateButton.click();
		return new CALViolationPage(context).get();
	}

	public T activate()
	{
		activateButton.click();
		return from.get();
	}

	public void setDates(java.util.Calendar[] range)
	{
		fromDate.setDate(range[0], this);
		untilDate.setDate(range[1], this);
	}

	public void setDatesHidden(java.util.Calendar[] range)
	{
		fromDate.setDateHidden(range[0].getTime());
		untilDate.setDateHidden(range[1].getTime());
	}

	public CALActivatePage<T> activateFailure()
	{
		activateButton.click();
		return get();
	}

	public CALOverridePage activateWithOverride()
	{
		activateButton.click();
		return new CALOverridePage(context, (CALSummaryPage) from).get();
	}

	public boolean isDateError()
	{
		try
		{
			return driver.findElement(By.className("mandatory")).getText()
				.contains("'From' date must come before 'until'.");
		}
		catch( NotFoundException nfe )
		{
			return false;
		}
	}

	public boolean isViolation()
	{
		return new CALViolationPage(context).isLoaded();
	}

	public T okViolation()
	{
		return new CALViolationPage(context).get().okViolation(from);
	}

	public void setCourse(String courseName)
	{
		selectCourseButton.click();
		SelectCourseDialog scd = new SelectCourseDialog(context, "cala_selectCourseDialog").get();
		scd.searchSelectAndFinish(courseName, this);
		// Wait for element text??
	}

	public Calendar getFromDate()
	{
		return fromDate;
	}

	public void cancel()
	{
		cancelButton.click();
	}

	public Calendar getUntilDate()
	{
		return untilDate;
	}

	public boolean containsCourseSelection()
	{
		return isPresent(By.xpath("//select[@id='calact_cs']"));
	}

	public void setCitation(String citationName)
	{
		citation.selectByVisibleText(citationName);
	}

}
