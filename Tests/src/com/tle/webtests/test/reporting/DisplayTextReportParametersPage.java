package com.tle.webtests.test.reporting;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.reporting.AbstractReportWindow;

public class DisplayTextReportParametersPage extends
	AbstractReportWindow<DisplayTextReportPage, DisplayTextReportParametersPage>
{	
	@FindBy(id = "c1")
	private WebElement displayTextParam;
	@FindBy(id = "c2")
	private WebElement displayTextParamMulti;
	@FindBy(id = "c2r")
	private WebElement singleRightArrowButton;
	@FindBy(id = "c2_left")
	private WebElement leftSelect;
	@FindBy(id = "c3")
	private WebElement displayTextParamNullable;
	@FindBy(id = "c4")
	private WebElement regularParamList;
	@FindBy(id = "c5")
	private WebElement regularParamNonList;
	@FindBy(id = "report-params")
	private WebElement updateDiv;

	public DisplayTextReportParametersPage(PageContext context)
	{
		super(context, new DisplayTextReportPage(context));
	}

	public String getCollectionId(String name)
	{
		return leftSelect.findElement(By.xpath(".//option[text()=" + quoteXPath(name) + "]")).getAttribute("value");
	}

	public void setDisplayTextParam(String value)
	{
		new EquellaSelect(context, displayTextParam).selectByVisibleText(value);
	}

	public void setDisplayTextParamNullable(String value)
	{
		new EquellaSelect(context, displayTextParamNullable).selectByVisibleText(value);
	}

	public void setRegularParamList(String value)
	{
		new EquellaSelect(context, regularParamList).selectByVisibleText(value);
	}

	public void setRegularParamNonList(String value)
	{
		regularParamNonList.clear();
		regularParamNonList.sendKeys(value);
	}

	public void selectValueFromLeft(String value)
	{
		leftSelect.findElement(By.xpath(".//option[text()=" + quoteXPath(value) + "]")).click();
		singleRightArrowButton.click();
	}
}