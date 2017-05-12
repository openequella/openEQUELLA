package com.tle.webtests.pageobject.generic.component;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class SelectCourseDialog extends AbstractPage<SelectCourseDialog>
{
	private final String baseId;
	@FindBy(id = "{baseid}_q")
	private WebElement queryField;
	@FindBy(id = "{baseid}_s")
	private WebElement searchButton;
	@FindBy(id = "{baseid}_ok")
	private WebElement okButton;
	@FindBy(id = "{baseid}_close")
	private WebElement closeButton;
	@FindBy(id = "results")
	private WebElement resultsDiv;
	@FindBy(className = "resultlist")
	private WebElement resultsList;

	public SelectCourseDialog(PageContext context, String baseId)
	{
		super(context);
		this.baseId = baseId;
	}

	public String getBaseid()
	{
		return baseId;
	}

	@Override
	protected void checkLoadedElement()
	{
		ensureVisible(queryField, searchButton, okButton);
	}

	public SelectCourseDialog search(String query)
	{
		queryField.clear();
		queryField.sendKeys(query);
		WaitingPageObject<SelectCourseDialog> ajaxUpdateExpect = ajaxUpdateExpect(resultsDiv, resultsList);
		searchButton.click();
		ajaxUpdateExpect.get();
		waitForElement(By.xpath("id('" + baseId + "')//div[@id='results']//ul/li"));
		return get();
	}

	public boolean searchWithoutMatch(String query)
	{
		queryField.clear();
		queryField.sendKeys(query);
		WaitingPageObject<SelectCourseDialog> ajaxUpdateExpect = ajaxUpdateExpect(resultsDiv, resultsList);
		searchButton.click();
		ajaxUpdateExpect.get();
		waitForElement(By.xpath("id('" + baseId + "')//div[@id='results']//h4[text()]"));
		String text = driver.findElement(By.xpath("id('" + baseId + "')//div[@id='results']//h4[text()]")).getText();
		if( text.equals("Your search did not match any courses.") )
		{
			return true;
		}
		return false;
	}

	public boolean containsCourse(String course)
	{
		return !driver.findElements(getByForCourse(course)).isEmpty();
	}

	public SelectCourseDialog select(String course)
	{
		driver.findElement(getByForCourse(course)).click();
		return get();
	}

	private By getByForCourse(String course)
	{
		String xpath = "//ul[@id =\"" + baseId + "_c\"]/li/label[text() = " + quoteXPath(course) + "]/../input";
		return By.xpath(xpath);
	}

	public <T extends AbstractPage<T>> T searchSelectAndFinish(String course, WaitingPageObject<T> page)
	{
		search(course);
		return selectAndFinish(course, page);
	}

	public <T extends AbstractPage<T>> T selectAndFinish(String course, WaitingPageObject<T> page)
	{
		select(course);
		return finish(page);
	}

	public <T extends AbstractPage<T>> T finish(WaitingPageObject<T> page)
	{
		okButton.click();
		return page.get();
	}

	public <T extends AbstractPage<T>> T cancel(WaitingPageObject<T> page)
	{
		ExpectedCondition<Boolean> removalCondition = removalCondition(closeButton);
		closeButton.click();
		waiter.until(removalCondition);
		return page.get();
	}
}
