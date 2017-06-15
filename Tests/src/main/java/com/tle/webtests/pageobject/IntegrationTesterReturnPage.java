package com.tle.webtests.pageobject;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;

public class IntegrationTesterReturnPage extends AbstractPage<IntegrationTesterReturnPage>
{

	public IntegrationTesterReturnPage(PageContext context)
	{
		super(context, By.xpath("//div[@class='formrow']"));
	}

	public String getReturnedUrl()
	{
		return returnedRow("url");
	}

	public String returnedRow(String row)
	{
		return driver.findElement(
			By.xpath("//div[@class='formrow' and label[normalize-space(text())='" + row + ":']]/textarea"))
			.getAttribute("value");
	}

	public boolean isSuccess()
	{
		return isPresent(By.xpath("//textarea[text()='success']"));
	}
}
