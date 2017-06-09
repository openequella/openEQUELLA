package com.tle.webtests.pageobject.integration.blackboard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class BlackboardBuildingBlockListPage extends AbstractPage<BlackboardBuildingBlockListPage>
{

	public BlackboardBuildingBlockListPage(PageContext context)
	{
		super(context, BlackboardPageUtils.pageTitleBy("Installed Tools"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getIntegUrl()
			+ "webapps/blackboard/execute/plugInController?sortDir=ASCENDING&sortCol=Name&numResults=100&startIndex=0");
	}

	public BlackboardEquellaSettingsPage equellaSettings()
	{
		equellaAction("Settings");
		return new BlackboardEquellaSettingsPage(context).get();
	}

	public BlackboardBuildingBlockListPage activateEquella()
	{
		equellaAction("Set Available");
		waitForElement(By.name("bottom_Approve"));
		driver.findElement(By.name("bottom_Approve")).click();
		waitForMsg();

		return get();
	}

	private void waitForMsg()
	{
		waitForElement(By.id("goodMsg1"));
		driver.findElement(By.xpath("id('inlineReceipt_good')//a")).click();
	}

	private WebElement equellaRow()
	{
		return driver.findElement(By.xpath("//tr[./th[contains(., 'EQUELLA Plugin')]]"));
	}

	public boolean isEquellaInstalled()
	{
		return !driver.findElements(By.xpath("//tr[./th[contains(., 'EQUELLA Plugin')]]")).isEmpty();
	}

	public BlackboardBuildingBlockListPage deleteEquella()
	{
		if( isEquellaInstalled() )
		{
			equellaAction("Uninstall");
			acceptConfirmation();
			waitForMsg();
		}
		return get();
	}

	private void equellaAction(String action)
	{
		WebElement equellaRow = equellaRow();
		equellaRow.findElement(By.xpath(".//a[contains(@title, 'Options Menu: Name')]")).click();
		WebElement cm = waitForElement(By.className("cmdiv"));
		waitForElement(cm, By.linkText(action)).click();
	}
}
