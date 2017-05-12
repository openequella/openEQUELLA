package com.tle.webtests.pageobject.selection;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

public class SelectionCheckoutPage extends AbstractPage<SelectionCheckoutPage>
{
	@FindBy(id = "_finishButton")
	private WebElement finishButton;
	@FindBy(id = "_cancelButton")
	private WebElement cancelButton;
	@FindBy(id = "vs_vs_0")
	private WebElement versionSelect;

	public SelectionCheckoutPage(PageContext context)
	{
		super(context, By.id("checkout-div"));
	}

	public <T extends PageObject> T returnSelection(WaitingPageObject<T> returnTo)
	{
		return returnSelection(null, returnTo);
	}

	public <T extends PageObject> T returnSelection(String frameName, WaitingPageObject<T> returnTo)
	{
		finishButton.click();
		driver.switchTo().defaultContent();
		if( frameName != null )
		{
			driver.switchTo().frame(frameName);
		}
		return returnTo.get();
	}

	public <T extends PageObject> T cancelSelection(WaitingPageObject<T> returnTo)
	{
		cancelButton.click();
		waiter.until(ExpectedConditions2.acceptAlert());
		driver.switchTo().defaultContent();
		return returnTo.get();
	}

	public <T extends PageObject> T returnSelection(WaitingPageObject<T> returnTo, WebElement frame)
	{
		driver.findElement(By.id("_finishButton")).click();
		driver.switchTo().defaultContent();
		driver.switchTo().frame(frame);
		return returnTo.get();
	}

	public boolean hasVersionSelection()
	{
		return isPresent(By.id("vs_vs_0"));
	}

	public String versionSelected()
	{
		return new EquellaSelect(context, versionSelect).getSelectedText();
	}

}
