package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;

import static com.tle.webtests.framework.Assert.assertEquals;


public class LoginNoticePage extends AbstractPage<LoginNoticePage>
{

	public LoginNoticePage(PageContext context)
	{
		super(context);
	}

	@Override
	public void checkLoaded() throws Error
	{
		if (context.getTestConfig().isAlertSupported())
		{
			driver.switchTo().alert();
		}
	}

	public HomePage acceptNotice()
	{
		return new HomePage(context).get();
	}

	public void assertNotice(String loginNotice)
	{
		if (context.getTestConfig().isAlertSupported())
		{
			assertEquals(driver.switchTo().alert().getText(), loginNotice);
		}
	}
}
