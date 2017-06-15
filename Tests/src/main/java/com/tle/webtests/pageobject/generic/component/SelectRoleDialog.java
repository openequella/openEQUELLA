package com.tle.webtests.pageobject.generic.component;

import java.text.MessageFormat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class SelectRoleDialog extends AbstractPage<SelectRoleDialog>
{
	private final String baseId;
	@FindBy(id = "{baseid}_sr_q")
	private WebElement queryField;
	@FindBy(id = "{baseid}_sr_s")
	private WebElement searchButton;
	@FindBy(id = "{baseid}_ok")
	private WebElement okButton;
	@FindBy(id = "{baseid}_cancel")
	private WebElement cancelButton;
	@FindBy(id = "results")
	private WebElement resultsDiv;
	@FindBy(className = "resultlist")
	private WebElement resultsList;

	public SelectRoleDialog(PageContext context, String baseId)
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

	public SelectRoleDialog search(String query)
	{
		queryField.clear();
		queryField.sendKeys(query);
		WaitingPageObject<SelectRoleDialog> ajaxUpdateExpect = ajaxUpdateExpect(resultsDiv, resultsList);
		searchButton.click();
		ajaxUpdateExpect.get();
		waitForElement(By.xpath("id('" + baseId + "')//div[@id='results']//ul/li"));
		return get();
	}

	public boolean searchWithoutMatch(String query)
	{
		queryField.clear();
		queryField.sendKeys(query);
		WaitingPageObject<SelectRoleDialog> ajaxUpdateExpect = ajaxUpdateExpect(resultsDiv, resultsList);
		searchButton.click();
		ajaxUpdateExpect.get();
		waitForElement(By.xpath("id('" + baseId + "')//div[@id='results']//h4[text()]"));
		String text = driver.findElement(By.xpath("id('" + baseId + "')//div[@id='results']//h4[text()]")).getText();
		if( text.equals("Your search did not match any roles.") )
		{
			return true;
		}
		return false;
	}

	public boolean containsRolename(String rolename)
	{
		return !driver.findElements(getByForRolename(rolename)).isEmpty();
	}

	public void select(String rolename)
	{
		driver.findElement(getByForRolename(rolename)).click();
	}

	private By getByForRolename(String rolename)
	{
		String xpath = MessageFormat.format("id({0})//div[@id={1}]//ul/li[div[contains(text(), {2})]]/input",
			quoteXPath(baseId), quoteXPath("results"), quoteXPath(rolename));
		return By.xpath(xpath);
	}

	public <T extends AbstractPage<T>> T selectAndFinish(String rolename, WaitingPageObject<T> page)
	{
		select(rolename);
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
