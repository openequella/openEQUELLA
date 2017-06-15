package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.StringSelectedStuff;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;

public class AutoCompleteTermControl extends NewAbstractWizardControl<AutoCompleteTermControl>
{
	@FindBy(id = "{wizid}dautocompleteControl")
	private WebElement controlElem;
	@FindBy(id = "{wizid}d_s")
	private WebElement selectButton;
	@FindBy(id = "{wizid}d_e")
	private WebElement termField;
	@FindBy(id = "{wizid}d_t")
	private WebElement selectionsTable;
	private WebDriverWait acWaiter;

	public AutoCompleteTermControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
		acWaiter = new WebDriverWait(driver, context.getTestConfig().getStandardTimeout(), 600);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return controlElem;
	}

	public void addNewTerm(String term)
	{
		termField.sendKeys(term);
		WaitingPageObject<StringSelectedStuff> waiter = getSelections().selectionWaiter(term);
		selectButton.click();
		waiter.get();
	}

	public WizardPageTab selectExistingTerm(String prefix, WizardPageTab wizardPage)
	{
		return selectExistingTerm(prefix, wizardPage, 1);
	}

	public WizardPageTab selectExistingTerm(String prefix, WizardPageTab wizardPage, int number)
	{
		waiter.until(ExpectedConditions2.elementAttributeToContain(termField, "class", "ui-autocomplete-input"));
		termField.clear();
		termField.sendKeys(prefix);

		AutoCompleteTermResults results = listWait();
		WaitingPageObject<AutoCompleteTermControl> control = ajaxUpdateExpect(controlElem, selectionsTable);
		results.selectByIndex(number);
		control.get();
		return wizardPage.get();
	}

	public String getAddedTermByIndex(int index)
	{
		return selectionsTable.findElement(By.xpath("//tbody/tr[" + index + "]/td[1]")).getText();
	}

	public AutoCompleteTermControl selectNothing()
	{
		WaitingPageObject<AutoCompleteTermControl> waiter = updateWaiter(selectionsTable);
		selectButton.click();
		return waiter.get();
	}

	public StringSelectedStuff getSelections()
	{
		return new StringSelectedStuff(context, controlElem);
	}

	public void removeTerm(String term)
	{
		StringSelectedStuff selections = getSelections();
		selections.clickActionWithConfirm(term, "Remove", Boolean.TRUE, selections.removalWaiter(term));
	}

	private AutoCompleteTermResults listWait()
	{
		acWaiter.until(new ExpectedCondition<Boolean>()
		{
			private String lastQuery;

			@Override
			public Boolean apply(WebDriver driver)
			{

				boolean loaded = new AutoCompleteTermResults(AutoCompleteTermControl.this).isLoaded();
				if( Check.isEmpty(lastQuery) && loaded )
				{
					return true;
				}

				if( Check.isEmpty(lastQuery) )
				{
					lastQuery = termField.getAttribute("value");
					termField.sendKeys(Keys.ESCAPE);
					termField.clear();
					((JavascriptExecutor) driver).executeScript("$(arguments[0]).keydown();", termField);
				}
				else
				{
					termField.sendKeys(lastQuery);
					lastQuery = null;
				}

				return false;
			}
		});

		return new AutoCompleteTermResults(this).get();
	}
}
