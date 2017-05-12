package com.tle.webtests.framework;

import org.openqa.selenium.WebDriver;

public interface WebDriverPool
{
	WebDriver getDriver();

	void releaseDriver(WebDriver driver);
}
