package com.tle.webtests.pageobject.generic.component;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class MultiLingualEditbox extends AbstractPage<MultiLingualEditbox>
{
	private final String baseId;
	private final boolean multiline;

	@FindBy(id = "{baseId}")
	private WebElement div;
	@FindBy(xpath = "id('{baseId}')//div[contains(@class, 'universaltranslation')]")
	private WebElement universalTranslation;
	@FindBy(xpath = "id('{baseId}')//div[contains(@class, 'alltranslations')]")
	private WebElement allTranslations;
	@FindBy(xpath = "id('{baseId}')//a[contains(@class,'collapse')]")
	private WebElement collapseLink;

	public MultiLingualEditbox(PageContext context, String baseId)
	{
		super(context, By.id(baseId));
		this.baseId = baseId;
		this.multiline = false;
	}

	public MultiLingualEditbox(PageContext context, WebElement div)
	{
		this(context, div, false);
	}

	public MultiLingualEditbox(PageContext context, WebElement div, boolean multiline)
	{
		super(context);
		this.baseId = null;
		this.div = div;
		this.multiline = multiline;
	}

	@Override
	public WebElement findLoadedElement()
	{
		return div;
	}

	public void setSelectedLanguage(String language)
	{
		getDropDown().selectByVisibleText(language);
		//I'm not sure there is anything we *can* wait for here.  It changes contents via JS.
		//This one weird time I'm going to do a sleep. Yeah, you heard right.
		sleepyTime(1000);
	}

	private WebElement getField()
	{
		WebElement field;
		if( !div.findElements(By.xpath(".//div[@class='singletranslation']")).isEmpty() )
		{
			field = multiline ? div.findElement(By.xpath(".//textarea")) : div.findElement(By.xpath(".//input"));
		}
		else
		{
			field = multiline ? div.findElement(By.xpath(".//textarea[@class='universalinput']")) : div.findElement(By
				.xpath(".//input[@class='universalinput']"));
		}
		return field;
	}

	public String getCurrentString()
	{
		WebElement field = getField();
		return field.getAttribute("value");
	}

	public void setCurrentString(String val)
	{
		WebElement field = getField();
		field.clear();
		field.sendKeys(val);
	}

	public void allMode()
	{
		getDropDown().selectByValue("all");
		getWaiter().until(ExpectedConditions.visibilityOf(allTranslations));
	}

	public void singleMode()
	{
		collapseLink.click();
		getWaiter().until(ExpectedConditions.visibilityOf(universalTranslation));
	}

	public void editLangString(String language, String value)
	{
		WebElement field;
		if( multiline )
		{
			field = allTranslations.findElement(By.xpath(".//label[text()=" + quoteXPath(language)
				+ "]/following-sibling::textarea"));
		}
		else
		{
			field = allTranslations.findElement(By.xpath(".//label[text()=" + quoteXPath(language)
				+ "]/following-sibling::input"));
		}
		field.clear();
		field.sendKeys(value);
	}

	public WebElement getDropDownSelect()
	{
		return driver.findElement(By.id(baseId + "_loc"));
	}

	public String getBaseId()
	{
		return baseId;
	}

	public EquellaSelect getDropDown()
	{
		return new EquellaSelect(context, getDropDownSelect());
	}
}
