package com.tle.webtests.test.reporting;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractReport;

public class UsersReportPage extends AbstractReport<UsersReportPage> 
{	
	public UsersReportPage(PageContext context)
	{
		super(context, By.xpath("//th/div[text()='username']"));
	}
	
	public String getUserName(int row)
	{
		return driver.findElement(By.xpath("id('__bookmark_1')//tbody//tr["+(row+2)+"]/td[2]/div")).getText();
	}
	
	public void openSubReport(String reportLink)
	{
		driver.findElement(By.linkText(reportLink)).click();
	}
	
	
}