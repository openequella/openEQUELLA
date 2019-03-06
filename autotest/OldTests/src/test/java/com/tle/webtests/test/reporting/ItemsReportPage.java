package com.tle.webtests.test.reporting;


import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractReport;

public class ItemsReportPage extends AbstractReport<ItemsReportPage> 
{	
	public ItemsReportPage(PageContext context)
	{
		super(context, By.xpath("//th/div[text()='Item Name']"));
	}
	
	public String getItemName()
	{
	    return driver.findElement(By.xpath("id('__bookmark_1')//tbody//tr[2]/td/div")).getText();
	}
	
}