package com.tle.webtests.pageobject.generic.component;

import java.text.MessageFormat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class SelectGroupDialog extends AbstractPage<SelectGroupDialog>
{
	private final String baseId;
	@FindBy(id = "{baseid}_sg_q")
	private WebElement queryField;
	@FindBy(id = "{baseid}_sg_s")
	private WebElement searchButton;
	@FindBy(id = "{baseid}_ok")
	private WebElement okButton;
	@FindBy(id = "{baseid}_cancel")
	private WebElement cancelButton;
	@FindBy(id = "results")
	private WebElement resultsDiv;
	@FindBy(className = "resultlist")
	private WebElement resultsList;

	public SelectGroupDialog(PageContext context, String baseId)
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

	public SelectGroupDialog search(String query)
	{
		queryField.clear();
		queryField.sendKeys(query);
		WaitingPageObject<SelectGroupDialog> ajaxUpdateExpect = ajaxUpdateExpect(resultsDiv, resultsList);
		searchButton.click();
		ajaxUpdateExpect.get();
		waitForElement(By.xpath("id('" + baseId + "')//div[@id='results']//ul/li"));
		return get();
	}

	public boolean searchWithoutMatch(String query)
	{
		queryField.clear();
		queryField.sendKeys(query);
		WaitingPageObject<SelectGroupDialog> ajaxUpdateExpect = ajaxUpdateExpect(resultsDiv, resultsList);
		searchButton.click();
		ajaxUpdateExpect.get();
		waitForElement(By.xpath("id('" + baseId + "')//div[@id='results']//h4[text()]"));
		String text = driver.findElement(By.xpath("id('" + baseId + "')//div[@id='results']//h4[text()]")).getText();
		if( text.equals("Your search did not match any groups.") )
		{
			return true;
		}
		return false;
	}

	public boolean containsGroupname(String groupname)
	{
		return !driver.findElements(getByForGroupname(groupname)).isEmpty();
	}

	public void select(String groupname)
	{
		driver.findElement(getByForGroupname(groupname)).click();
	}

	private By getByForGroupname(String groupname)
	{
		String xpath = MessageFormat.format("id({0})//div[@id={1}]//ul/li[div[text()={2}]]/input", quoteXPath(baseId),
			quoteXPath("results"), quoteXPath(groupname));
		return By.xpath(xpath);
	}

	public <T extends AbstractPage<T>> T selectAndFinish(String groupname, WaitingPageObject<T> page)
	{
		select(groupname);
		return finish(page);
	}

	public <T extends AbstractPage<T>> T finish(WaitingPageObject<T> page)
	{
		okButton.click();
		return page.get();
	}

	public <T extends AbstractPage<T>> T cancel(WaitingPageObject<T> page)
	{
		ExpectedCondition<Boolean> removalCondition = removalCondition(cancelButton);
		cancelButton.click();
		waiter.until(removalCondition);
		return page.get();
	}
}
