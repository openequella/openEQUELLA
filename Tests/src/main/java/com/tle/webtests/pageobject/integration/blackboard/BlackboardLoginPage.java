package com.tle.webtests.pageobject.integration.blackboard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class BlackboardLoginPage extends AbstractPage<BlackboardLoginPage>
{
	@FindBy(id = "user_id")
	private WebElement user;
	@FindBy(id = "password")
	private WebElement pass;
	@FindBy(xpath = "//input[@value='Login']")
	private WebElement login;

	public BlackboardLoginPage(PageContext context)
	{
		super(context, By.id("loginBox"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getIntegUrl() + "/webapps/login?action=logout");
	}

	public BlackboardMyInstitutionPage logon(String username, String password)
	{
		user.clear();
		user.sendKeys(username);
		pass.clear();
		pass.sendKeys(password);
		login.click();
		return new BlackboardMyInstitutionPage(context).get();
	}

}
