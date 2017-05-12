package com.tle.webtests.pageobject.migration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class MigrationPage extends AbstractPage<MigrationPage>
{

	public MigrationPage(PageContext context)
	{
		super(context, By.id("content-body"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "migration.do");
	}

	public void runMigrations(String password)
	{
		if( isPresent(By.id("mp_password")) )
		{
			WebElement pass = driver.findElement(By.id("mp_password"));
			pass.clear();
			pass.sendKeys(password);
			driver.findElement(By.id("i0")).click();
			new UpgradeStepsPage(context).get().upgrade(password);
		}
	}

}
