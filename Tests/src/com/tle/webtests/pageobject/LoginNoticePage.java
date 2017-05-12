package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;

public class LoginNoticePage extends AbstractPage<LoginNoticePage>
{

	public LoginNoticePage(PageContext context)
	{
		super(context);
	}

	@Override
	public void checkLoaded() throws Error
	{
		driver.switchTo().alert();
	}

	public String getNoticeText()
	{
		return driver.switchTo().alert().getText();
	}

	public HomePage acceptNotice()
	{
		driver.switchTo().alert().accept();
		return new HomePage(context).get();
	}

	public LoginPage cancelNotice()
	{
		driver.switchTo().alert().dismiss();
		return new LoginPage(context).get();
	}
}
