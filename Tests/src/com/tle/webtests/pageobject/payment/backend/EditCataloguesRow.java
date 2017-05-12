package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.tle.webtests.pageobject.AbstractPage;

public class EditCataloguesRow extends AbstractPage<EditCataloguesRow>
{
	private static final String CMD_EXCLUDE = "Exclude from catalogue";
	private static final String CMD_REMOVE_WHITELIST = "Remove from manual additions";
	private static final String CMD_ADD = "Add to catalogue";

	public EditCataloguesRow(ItemCataloguesPage parent, SearchContext table, String name)
	{
		super(parent.getContext(), By.xpath(".//tr[td[normalize-space(text())=" + quoteXPath(normaliseSpace(name))
			+ "]]"));
		relativeTo = table;
	}

	public boolean isAddAvailable()
	{
		return isCommandAvailable(CMD_ADD);
	}

	public boolean isRemoveManualAvailable()
	{
		return isCommandAvailable(CMD_REMOVE_WHITELIST);
	}

	private boolean isCommandAvailable(String command)
	{
		return isPresent(loadedElement, commandBy(command));
	}

	private By commandBy(String command)
	{
		return By.xpath("./td[4]/a[text()=" + quoteXPath(command) + "]");
	}

	public Alert clickAdd()
	{
		return clickAlertCommand(CMD_ADD);
	}

	private void clickCommand(String command)
	{
		loadedElement.findElement(commandBy(command)).click();
	}

	private Alert clickAlertCommand(String command)
	{
		clickCommand(command);
		return driver.switchTo().alert();
	}

	public Alert clickRemoveWhiteList()
	{
		return clickAlertCommand(CMD_REMOVE_WHITELIST);
	}

	public boolean isExcludeAvailable()
	{
		return isCommandAvailable(CMD_EXCLUDE);
	}

	public boolean isRemoveExcludeAvailable()
	{
		return isCommandAvailable("Remove from exclusions");
	}

	public String getStatus()
	{
		return loadedElement.findElement(By.xpath("./td[3]")).getText();
	}

	public Alert clickExclude()
	{
		return clickAlertCommand(CMD_EXCLUDE);
	}

	public boolean isIncluded()
	{
		return isPresent(loadedElement, By.xpath(".//img[@title='Included']"));
	}

	public boolean isExcluded()
	{
		return isPresent(loadedElement, By.xpath(".//img[@title='Excluded']"));
	}

}
