package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectedConditions2;

public class ChromeHacks
{

	// chrome cant switch to object frames
	public static ExpectedCondition<WebDriver> convertObjectToiFrame(PageContext context, WebElement objectTag)
	{
		String id = objectTag.getAttribute("id");
		if( objectTag.getTagName().equals("iframe") )
		{
			return ExpectedConditions2.frameToBeAvailableAndSwitchToIt(context.getDriver(), By.id(id));
		}
		JavascriptExecutor js = ((JavascriptExecutor) context.getDriver());
		String url = objectTag.getAttribute("data");
		String height = objectTag.getAttribute("height");
		String width = objectTag.getAttribute("width");
		String iframe;

		if( !Check.isEmpty(width) && !Check.isEmpty(height) )
		{
			iframe = "<iframe width=" + width + " height=" + height + " id='" + id + "' src='" + url + "'></iframe>";
		}
		else
		{
			iframe = "<iframe id='" + id + "' src='" + url + "'></iframe>";
		}

		js.executeScript("arguments[0].parentElement.innerHTML = arguments[1];", objectTag, iframe);

		return ExpectedConditions2.frameToBeAvailableAndSwitchToIt(context.getDriver(), By.id(id));
	}
}
