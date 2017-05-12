package com.tle.webtests.framework;

import org.openqa.selenium.By;

import com.tle.webtests.pageobject.AbstractPage;

public class EBy
{
	public static By buttonText(String text)
	{
		return By.xpath(".//button[normalize-space(child::text()) = " + AbstractPage.quoteXPath(text) + "]");
	}

	public static By linkText(String text)
	{
		return By.xpath(".//a[normalize-space(child::text()) = " + AbstractPage.quoteXPath(text) + "]");
	}
}
