package com.tle.webtests.pageobject;

import org.openqa.selenium.support.ui.WebDriverWait;

import com.tle.webtests.framework.PageContext;

public interface WaitingPageObject<T extends PageObject>
{
	T get();

	WebDriverWait getWaiter();

	PageContext getContext();
}
