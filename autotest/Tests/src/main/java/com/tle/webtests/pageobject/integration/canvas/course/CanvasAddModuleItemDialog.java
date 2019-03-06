package com.tle.webtests.pageobject.integration.canvas.course;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.selection.SelectionSession;

public class CanvasAddModuleItemDialog extends AbstractPage<CanvasAddModuleItemDialog>
{
	@FindBy(id = "add_module_item_select")
	private WebElement selectTypeElement;
	@FindBy(xpath = "//button[normalize-space(text())='Add Item']")
	private WebElement saveButton;

	private Select selectTypeDropdown;

	public CanvasAddModuleItemDialog(PageContext context)
	{
		// FIXME get better findBy
		super(context, By.id("ui-id-2"));
		selectTypeDropdown = new Select(selectTypeElement);
	}

	public SelectionSession startSelectionSession(String toolName)
	{
		selectTypeDropdown.selectByValue("context_external_tool");
		waiter.until(ExpectedConditions.presenceOfElementLocated(By.xpath(getToolXpath(toolName))));
		driver.findElement(By.xpath(getToolXpath(toolName))).click();
		driver.switchTo().frame(waitForElement(By.id("resource_selection_iframe")));
		return new SelectionSession(context).get();
	}

	private String getToolXpath(String toolName)
	{
		return "//li[@class='tool resource_selection']/a[text() = " + quoteXPath(toolName) + "]";
	}

	public CanvasModulePage add(CanvasModulePage modules)
	{
		String buttonXpath = "//div[@class='ui-dialog-buttonset']/button[contains(@class,'add_item_button')]";
		waiter.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(buttonXpath)));
		driver.findElement(By.xpath(buttonXpath)).click();
		return modules.get();
	}

}
