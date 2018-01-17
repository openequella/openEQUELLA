package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class ModerationMessagePage<M extends AbstractWizardTab<M>> extends AbstractWizardTab<M>
{
	@FindBy(id = "{pfx}_commentField")
	private WebElement messageField;
	@FindBy(id = "{pfx}_rejectSteps")
	private WebElement stepList;
	@FindBy(id = "{pfx}_ok")
	private WebElement okButton;
	@FindBy(id = "{pfx}_c")
	private WebElement cancelButton;

	public ModerationMessagePage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return messageField;
	}

	public abstract String getPfx();

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

	public ModerationView addModerationComment(String message)
	{
		messageField.clear();
		messageField.sendKeys(message);

		ModerationView moderationView = new ModerationView(context);
		moderationView.checkLoaded();
		WaitingPageObject<ModerationView> modViewWaiter = moderationView.updateWaiter();
		okButton.click();
		return modViewWaiter.get();
	}

	public ModerationView cancel()
	{
		By dialogById = By.id(getPfx());
		WebElement dialogElem = driver.findElement(dialogById);
		cancelButton.click();
		WebDriverWait waiter = getWaiter();
		waiter.until(ExpectedConditions.stalenessOf(dialogElem));
		waiter.until(ExpectedConditions.invisibilityOfElementLocated(dialogById));
		return new ModerationView(context).get();
	}

}
