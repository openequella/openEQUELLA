package com.tle.webtests.pageobject.payment.storefront;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class PurchaseDetailsTable extends AbstractPage<PurchaseDetailsTable>
{
	private WebElement detailsTable;

	public PurchaseDetailsTable(PageContext context, By loadBy)
	{
		super(context, loadBy);
		detailsTable = driver.findElement(loadBy);
	}

	public String getBuyer()
	{
		return detailsTable.findElement(By.xpath("./tbody/tr/td[2]/span")).getText();
	}

	public String getPricePaid()
	{
		return detailsTable.findElement(By.xpath("./tbody/tr/td[3]")).getText();
	}

	public String getUsers()
	{
		return detailsTable.findElement(By.xpath("./tbody/tr/td[4]")).getText();
	}

	public boolean startDateExists()
	{
		return !detailsTable.findElement(By.xpath("./tbody/tr/td[5]")).getText().equals("");
	}

}
