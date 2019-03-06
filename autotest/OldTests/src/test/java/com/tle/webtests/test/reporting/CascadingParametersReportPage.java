
package com.tle.webtests.test.reporting;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractReport;

public class CascadingParametersReportPage extends AbstractReport<CascadingParametersReportPage>
{
	public CascadingParametersReportPage(PageContext context)
	{
		super(context, By.xpath("//div[text()='Selected description']"));
	}
	
	public String getReportValue()
	{
		return driver.findElement(By.xpath("//tbody//tr[2]/td/div[2]")).getText();
	}
}
