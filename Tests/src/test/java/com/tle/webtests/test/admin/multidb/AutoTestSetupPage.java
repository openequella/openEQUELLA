package com.tle.webtests.test.admin.multidb;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class AutoTestSetupPage extends AbstractPage<AutoTestSetupPage>
{
	@FindBy(xpath = "//button[text() = 'Clear filestore and freetext']")
	private WebElement clearIt;
	
	public AutoTestSetupPage(PageContext context)
	{
		super(context);
	}
	
	@Override
	protected void loadUrl()
	{
		get("institutions.do", "is.admin", "true", "is.autotest", "true");
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return clearIt;
	}
	
	public AutoTestSetupPage clearData()
	{
		WaitingPageObject<AutoTestSetupPage> update = updateWaiter();
		clearIt.click();
		return update.get();
	}
}
