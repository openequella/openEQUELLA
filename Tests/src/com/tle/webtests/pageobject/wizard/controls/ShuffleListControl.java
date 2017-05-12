package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class ShuffleListControl extends AbstractWizardControl<ShuffleListControl>
{
	@FindBy(id = "{wizid}_addButton")
	private WebElement addButton;
	@FindBy(id = "{wizid}_text")
	private WebElement textField;
	@FindBy(id = "{wizid}_div")
	private WebElement div;

	public ShuffleListControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return textField;
	}

	protected By getRowForValue(String value)
	{
		return By.xpath("//tr/td[@class='shuffle-text' and text()=" + quoteXPath(value) + "]/..");
	}

	public void add(String value)
	{
		textField.clear();
		textField.sendKeys(value);
		addButton.click();
		waitForElement(getRowForValue(value));
	}

	public void remove(String value)
	{
		div.findElement(getRowForValue(value)).findElement(By.className("shuffle-remove")).click();
	}

	public void edit(String value)
	{
		div.findElement(getRowForValue(value)).findElement(By.className("shuffle-edit")).click();
	}

	public boolean isDisabled()
	{
		return (" " + div.getAttribute("class").trim()).contains(" disabled");
	}

}
