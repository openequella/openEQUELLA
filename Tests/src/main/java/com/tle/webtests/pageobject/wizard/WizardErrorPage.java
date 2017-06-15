package com.tle.webtests.pageobject.wizard;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class WizardErrorPage extends AbstractPage<WizardErrorPage>
{

	public WizardErrorPage(PageContext context)
	{
		super(context, By.xpath("//h2[text()='An error occurred in the wizard']"));
	}

	public String getError()
	{
		return driver.findElement(By.cssSelector("div.error p")).getText();
	}

}
