package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class SalesHistoryPage extends AbstractPage<SalesHistoryPage>
{

	public SalesHistoryPage(PageContext context)
	{
		super(context, By.xpath("//div[@id='col1']/div/h2[text()='Sales history']"));
	}

	private WebElement getRow(int index)
	{
		return driver.findElement(By.xpath("//table[@id='shc_historyTable']/tbody/tr[" + index + "]"));
	}

	public String getStoreFrontForIndex(int index)
	{
		return getRow(index).findElement(By.xpath(".//td[2]")).getText();
	}

	public String getTransactionForIndex(int index)
	{

		return getRow(index).findElement(By.xpath(".//td[3]")).getText();
	}

	public String getPriceForIndex(int index)
	{

		return getRow(index).findElement(By.xpath(".//td[4]")).getText();
	}

	public boolean hasNoHistory()
	{
		return getRow(1).findElement(By.xpath(".//td[1]")).getText().equals("No sales history found for this item");
	}

}
