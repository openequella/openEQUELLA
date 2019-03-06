package com.tle.webtests.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

public class ReceiptPage
{
	private static final By BY_RECEIPT = By.xpath("//div[@id='receipt-message']/span");
	private static final By BY_CLOSE = By.xpath("//div[@id='receipt-message']/button");

	public static <T extends PageObject> WaitingPageObject<T> waiter(String text, WaitingPageObject<T> pageObject)
	{
		return ExpectWaiter.waiter(condition(text), pageObject);
	}

	public static <T extends PageObject> WaitingPageObject<T> waiterContains(String text,
		WaitingPageObject<T> pageObject)
	{
		return ExpectWaiter.waiter(containsCondition(text), pageObject);
	}

	public static ExpectedCondition<?> condition(String text)
	{
		return ExpectedConditions2.textToEqualInElement(null, BY_RECEIPT, text);
	}

	public static ExpectedCondition<?> containsCondition(String text)
	{
		return ExpectedConditions.textToBePresentInElementLocated(BY_RECEIPT, text);
	}

	public static void dismiss(WebDriverWait waiter)
	{
		waiter.until(new Function<WebDriver, Boolean>() {

			@Override
			public Boolean apply(WebDriver driver) {
				WebElement closeButton = driver.findElement(BY_CLOSE);
				closeButton.click();
				return ExpectedConditions.invisibilityOf(closeButton).apply(driver);
			}			
		});
	}
}
