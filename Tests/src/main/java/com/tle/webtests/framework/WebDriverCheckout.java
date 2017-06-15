package com.tle.webtests.framework;

import org.openqa.selenium.WebDriver;

public class WebDriverCheckout
{
	private final WebDriver driver;

	public WebDriverCheckout(WebDriver webDriver)
	{
		this.driver = webDriver;
	}

	public WebDriver getDriver()
	{
		return driver;
	}
}