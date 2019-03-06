package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.SubWizardPage;

public class RepeaterControl extends AbstractWizardControl<RepeaterControl>
{
	private WebElement getGroups()
	{
		return byWizId("_groups");
	}

	private WebElement getAddButton()
	{
		return byWizId("_addButton");
	}

	public RepeaterControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
		setMustBeVisible(false);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getGroups();
	}

	private int groupCount()
	{
		return getGroups().findElements(By.xpath("div[contains(@class, 'repeater')]")).size();
	}

	private By getByForIndex(int index)
	{
		return By.xpath("div[position() = " + index + " and contains(@class, 'repeater')]");
	}

	public SubWizardPage getControls(int treenum, int ctrlOffset)
	{
		return new SubWizardPage(context, page, treenum, ctrlOffset);
	}

	public SubWizardPage add(int treenum, int ctrlOffset)
	{
		SubWizardPage subWizard = getControls(treenum, ctrlOffset);
		ExpectWaiter<SubWizardPage> controlWaiter = ExpectWaiter.waiter(
			ExpectedConditions2.visibilityOfElementLocated(getGroups(), getByForIndex(groupCount() + 1)), subWizard);
		getAddButton().click();
		return controlWaiter.get();
	}

	public void remove(int index)
	{
		WebElement element = driver.findElement(By.id(page.getControlId(ctrlnum) + "index" + index));
		ExpectedCondition<Boolean> removed = ExpectedConditions2.stalenessOrNonPresenceOf(element);
		element.click();
		waiter.until(removed);
	}

	public boolean isShowingMinError()
	{
		String text = getInvalidMessage();
		return text.contains("Please create at least");
	}

	public boolean isAddDisabled()
	{
		return !getAddButton().isEnabled();
	}

	public String getAddNoun()
	{
		return getAddButton().getText().substring(4);
	}

	public boolean isDisabled()
	{
		return isAddDisabled();
	}
}
