package com.tle.webtests.pageobject.wizard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;

public class ModerationMessagePage extends AbstractWizardTab<ModerationMessagePage>
{
	@FindBy(id = "_tasksc_commentField")
	private WebElement messageField;
	@FindBy(id = "_tasksc_rejectSteps")
	private WebElement stepList;
	@FindBy(id = "_tasksc_submitButton")
	private WebElement okButton;
	@FindBy(id = "_tasksc_cancelButton")
	private WebElement cancelButton;

	public ModerationMessagePage(PageContext context)
	{
		super(context, By.id("_tasksc_commentField"));
	}

	public TaskListPage rejectWithMessage(String message, String toStep)
	{
		messageField.clear();
		messageField.sendKeys(message);
		if( toStep != null )
		{
			new EquellaSelect(context, stepList).selectByVisibleText(toStep);
		}
		okButton.click();
		return ReceiptPage.waiter("Successfully rejected previous task", new TaskListPage(context)).get();
	}

	public TaskListPage acceptWithMessage(String message)
	{
		messageField.clear();
		messageField.sendKeys(message);
		okButton.click();
		return ReceiptPage.waiter("Successfully approved previous task", new TaskListPage(context)).get();
	}

	public ModerationMessagePage addModerationComment(String message)
	{
		messageField.clear();
		messageField.sendKeys(message);
		okButton.click();
		return new ModerationMessagePage(context).get();
	}

	public ModerationView cancel()
	{
		cancelButton.click();
		return new ModerationView(context).get();
	}

}
