package com.tle.webtests.pageobject.settings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class ManualDataFixesPage extends AbstractPage<ManualDataFixesPage>
{
	@FindBy(id = "rmdfsgto_execute")
	private WebElement generateThumbnailsButton;

	public ManualDataFixesPage(PageContext context)
	{
		super(context, By.id("rmdfsgto_execute"));
	}

	public void generateMissingThumnails()
	{
		generateThumbnailsButton.click();
		try
		{
			Thread.sleep(10000);
		}
		catch( InterruptedException e )
		{
			// don't care
		}
	}

}
