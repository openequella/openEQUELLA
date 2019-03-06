package com.tle.webtests.pageobject.generic.component;

import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.cal.EditActivationPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class SelectCourseDialog extends AbstractPage<SelectCourseDialog>
{
	private final String baseId;
	private ExpectedCondition<WebElement> fieldAvailable =
			ExpectedConditions.visibilityOfElementLocated(By.className("select2-search__field"));

	public SelectCourseDialog(PageContext context, String baseId)
	{
		super(context);
		this.baseId = baseId;
	}

	public String getBaseid()
	{
		return baseId;
	}

	public WebElement getBaseElement()
	{
		return driver.findElement(By.id("select2-"+baseId+"-container"));
	}

	@Override
	public WebElement findLoadedElement()
	{
		return getBaseElement();
	}

	public <T extends PageObject> T searchSelectAndFinish(String course,
															 WaitingPageObject<T> returnTo)
	{
		WebElement baseElement = getBaseElement();
		baseElement.click();
		WebElement searchField = getWaiter().until(fieldAvailable);
		searchField.sendKeys(course);
		WebElement entries = waiter.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(), " + quoteXPath(course) + ")]")));
		entries.click();
		return returnTo.get();
	}
}
