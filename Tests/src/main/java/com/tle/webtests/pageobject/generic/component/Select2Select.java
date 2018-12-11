package com.tle.webtests.pageobject.generic.component;

import com.tle.common.Utils;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Select2Select extends AbstractPage<Select2Select>
{
	private final WebElement origSelect;
	private WebElement textDiv;

	public Select2Select(PageContext context, WebElement select)
	{
		super(context, By.xpath("following-sibling::span[contains(@class, 'select2-container--default')]"));
		origSelect = select;
		setRelativeTo(origSelect);
		get();
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		WebElement container = origSelect.findElement(loadedBy);
		textDiv = container.findElement(By.xpath(".//span[contains(@class, 'select2-selection__rendered')]"));
	}

	public void selectByVisibleText(String name)
	{
		WebElement dropDiv = clickOn();
		WebElement entry = dropDiv.findElement(
			By.xpath(".//li[text()=" + quoteXPath(name) + "]"));
		scrollToElement(entry);
		entry.click();
	}

	public void selectByValue(String value)
	{
		WebElement option = origSelect.findElement(By.xpath(".//option[@value=" + quoteXPath(value) + "]"));
		String text = (String) ((JavascriptExecutor) driver).executeScript("return arguments[0].innerHTML;", option);
		text = Utils.unent(text);
		selectByVisibleText(text);
	}

	/**
	 * Weird behavior in the xpath: if the int parameter is 0, the resultant
	 * xpath ".//a[1]" acts as expected, but for any other value, the integer
	 * value must be quoted, to wit: ".//a['7']" or ".//a[position()='7']". An
	 * alternative to that lunacy is to retrieve the WebElements as a list, and
	 * then (having established list size lies within parameter size) use the
	 * list.get(n) accessor to click the targeted element. Selenium bug?
	 * 
	 * @param index
	 */
	public void selectByIndex(int index)
	{
		WebElement dropDiv = clickOn();
		try
		{
			dropDiv.findElement(By.xpath(".//li[" + (index + 1) + "]")).click();
		}
		catch( NoSuchElementException noseeum )
		{
			List<WebElement> allLinks = dropDiv.findElements(By.xpath(".//li"));
			if( allLinks.size() > index )
			{
				allLinks.get(index).click();
			}
			else
				throw noseeum;
		}
	}

	public List<WebElement> getOptionElements()
	{
		return origSelect.findElements(By.xpath(".//option"));
	}

	public String getSelectedValue()
	{
		List<WebElement> options = getOptionElements();
		String value = null;
		for( WebElement opt : options )
		{
			if( opt.isSelected() )
			{
				value = opt.getAttribute("value");
				break;
			}
		}
		return value;
	}

	public List<WebElement> getSelectableHyperinks()
	{
		List<WebElement> foundElements = clickOn().findElements(By.xpath(".//li"));
		// we may need to click again to close the list, but can't do it yet
		return foundElements;
	}

	public String getSelectedText()
	{
		return textDiv.getText();
	}

	public WebElement clickOn()
	{
		textDiv.click();
		return driver.findElement(By.xpath("/html/body/span/span[contains(@class, 'select2-dropdown')]"));
	}

	public boolean isDisabled()
	{
		return !origSelect.isEnabled();
	}
}
