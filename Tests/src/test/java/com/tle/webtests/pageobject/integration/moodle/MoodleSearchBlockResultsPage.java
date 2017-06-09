package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class MoodleSearchBlockResultsPage extends AbstractPage<MoodleSearchBlockResultsPage>
{

	@FindBy(id = "id_searchstring")
	private WebElement searchField;

	@FindBy(xpath = "//input[@value='Search']")
	private WebElement searchButton;

	public MoodleSearchBlockResultsPage(PageContext context)
	{
		super(context, By.id("id_searchstring"));
	}

	public MoodleSearchBlockResultsPage search(String search)
	{
		searchField.clear();
		searchField.sendKeys(search);
		searchButton.click();
		return get();
	}

	public boolean hasResults()
	{
		return !driver.findElements(
			By.xpath("//table[contains(@class, 'generaltable')]//tr[contains(@class,'lastrow')]")).isEmpty();
	}

	public String titleForResult(int i)
	{
		return driver.findElement(xpathFor(i, 1)).getText();
	}

	public String descriptionForResult(int i)
	{
		return driver.findElement(xpathFor(i, 2)).getText();
	}

	public String filenameForResult(int i)
	{
		return driver.findElement(xpathFor(i, 3)).getText();
	}

	public String urlForResult(int i)
	{
		return driver.findElement(xpathFor(i, 4)).findElement(By.xpath("./a")).getAttribute("href");
	}

	private By xpathFor(int i, int field)
	{
		return By.xpath("//table[contains(@class, 'generaltable')]//tr[contains(@class,'r')][" + i + "]/td[" + field
			+ "]");
	}

}
