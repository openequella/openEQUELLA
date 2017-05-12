package com.tle.webtests.test.reporting;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractReport;

public class FreeTextReportPage extends AbstractReport<FreeTextReportPage>
{
	@FindBy(id = "__bookmark_1")
	private WebElement basicTable;
	@FindBy(id = "__bookmark_5")
	private WebElement countDiv;
	@FindBy(id = "__bookmark_6")
	private WebElement listFileTable;
	@FindBy(id = "__bookmark_7")
	private WebElement matrixTable;
	@FindBy(id = "__bookmark_8")
	private WebElement matrixCountTable;

	ArrayList<String> itemResultNames = new ArrayList<String>();
	String itemThreeKey;

	public FreeTextReportPage(PageContext context)
	{
		super(context, By.xpath("//h1/b[text()='Items Starting with `Reporting`']"));
	}

	/*
	 * All item names should start with 'reporting'
	 */
	public boolean checkBasicReportResults()
	{
		List<WebElement> itemNames = basicTable.findElements(By.xpath("./tbody/tr/td[2]/div"));
		for( WebElement name : itemNames )
		{

			if( !name.getText().startsWith("Reporting") )
			{
				return false;
			}
			if(name.getText().contains("3"))
			{
				itemThreeKey = name.findElement(By.xpath("./../../td[1]")).getText();
			}
			itemResultNames.add(name.getText());
		}
		return true;
	}

	// should be 3 results
	public boolean checkBasicCountResults()
	{
		return countDiv.getText().equals("3");
	}

	// should only be Reporting Item 3 (one result + footer)
	public boolean checkListFilesResults()
	{
		return listFileTable.findElement(By.xpath("./tbody/tr/td[1]/div")).getText().equals(itemThreeKey);
	}

	// names returned should be same as basic query
	public boolean checkMatrixResults()
	{
		List<WebElement> itemNames = matrixTable.findElements(By.xpath("./tbody/tr/td[2]/div"));
		for( WebElement name : itemNames )
		{
			if( !itemResultNames.contains(name.getText()) )
			{
				return false;
			}
		}
		return true;
	}

	// should be 3 unique results (1 count each)
	public boolean checkMatrixCountResults()
	{
		List<WebElement> counts = matrixCountTable.findElements(By.xpath("./tbody/tr/td[3]/div"));
		for( WebElement count : counts )
		{
			if( !count.getText().equals("1") )
			{
				return false;
			}
		}
		return true;
	}

}
