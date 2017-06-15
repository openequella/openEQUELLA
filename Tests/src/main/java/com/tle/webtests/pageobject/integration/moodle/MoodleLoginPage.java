package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.UndeterminedPage;

public class MoodleLoginPage extends MoodleBasePage<MoodleLoginPage>
{
	@FindBy(id = "username")
	private WebElement user;
	@FindBy(id = "password")
	private WebElement pass;
	@FindBy(xpath = "//input[@id='loginbtn' or @value='Login']")
	private WebElement loginButton;

	public MoodleLoginPage(PageContext context)
	{
		super(context, By.xpath("//h2[contains(text(), 'Log in') or contains(text(), 'Returning to this web site')]"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getIntegUrl() + "login/index.php");
	}

	@Override
	public void checkLoaded() throws Error
	{
		String logoutText = "Logout";
		if( this.getMoodleVersion() >= 26 )
		{
			logoutText = "Log out";
		}
		if( isPresent(By.xpath("//input[@value='" + logoutText + "']")) )
		{
			driver.findElement(By.xpath("//input[@value='" + logoutText + "']")).click();
		}
		if( isPresent(By.xpath("//a[normalize-space(text())='" + logoutText + "']")) )
		{
			driver.findElement(By.xpath("//a[normalize-space(text())='" + logoutText + "']")).click();
			driver.get(context.getIntegUrl() + "login/index.php");
		}
		super.checkLoaded();
	}

	private void doLogin(String username, String password)
	{
		user.clear();
		user.sendKeys(username);
		pass.clear();
		pass.sendKeys(password);
		loginButton.click();
	}

	public UndeterminedPage<PageObject> logonToUndetermined(String username, String password, AbstractPage<?> page1,
		AbstractPage<?> page2)
	{
		doLogin(username, password);
		UndeterminedPage<PageObject> up = new UndeterminedPage<PageObject>(context, page1, page2);
		return up;
	}

	public MoodleIndexPage logon(String username, String password)
	{
		doLogin(username, password);
		return new MoodleIndexPage(context).get();
	}

	public MoodleSettingsUpgradePage logonToSettingsUpgrade(String username, String password)
	{
		doLogin(username, password);
		return new MoodleSettingsUpgradePage(context).get();
	}

	public MoodleAdminPage logonToPluginsCheck(String username, String password)
	{
		doLogin(username, password);
		return new MoodleAdminPage(context).get();
	}
}
