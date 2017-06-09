package com.tle.webtests.pageobject.payment.storefront;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class StoreRegistrationPage extends AbstractPage<StoreRegistrationPage>
{
	private static final String TITLE = "Store registrations";

	@FindBy(id = "ss_add")
	protected WebElement registerStoreLink;

	public StoreRegistrationPage(PageContext context)
	{
		super(context, By.xpath("//div[@id='entities']/h2[text()= " + quoteXPath(TITLE) + "]"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "/access/store.do");
	}

	public StoreRegistrationEditPage createRegistration()
	{
		registerStoreLink.click();
		return new StoreRegistrationEditPage(context, true).get();
	}

	public boolean checkEntryExists(String storeName)
	{
		List<WebElement> entries = driver.findElements(By.xpath("//table[@id='ss_ent']/tbody/tr/td[1]/a"));
		for( WebElement entry : entries )
		{
			if( entry.getText().equals(storeName) )
			{
				return true;
			}
		}

		return false;
	}

	public StoreRegistrationDetailsPage viewStoreDetail(String storeName)
	{
		driver.findElement(By.xpath("//table[@id='ss_ent']/tbody/tr/td[1]/a[text() = " + quoteXPath(storeName) + "]"))
			.click();
		return new StoreRegistrationDetailsPage(context).get();
	}

	public boolean isEntryDisabled(String storeName)
	{
		return isPresent(By.xpath("//table[@id='ss_ent']/tbody/tr[contains(@class,'disabled')]/td/a[text() = "
				+ quoteXPath(storeName) + "]"));
	}

	public StoreRegistrationPage disableStore(String storeName)
	{
		if( !isEntryDisabled(storeName) )
		{
			WebElement disableAction = driver.findElement(By.xpath("//table[@id='ss_ent']/tbody/tr/td/a[text() = "
				+ quoteXPath(storeName) + "]/../../td[2]/a[text() = 'Disable']"));
			WaitingPageObject<StoreRegistrationPage> waiter = removalWaiter(disableAction);
			disableAction.click();
			return waiter.get();
		}
		return this;
	}

	public StoreRegistrationPage deleteStore(String storeName)
	{
		if( isEntryDisabled(storeName) )
		{
			WebElement deleteAction = driver.findElement(By.xpath("//table[@id='ss_ent']/tbody/tr/td/a[text() = "
				+ quoteXPath(storeName) + "]/../../td[2]/a[text() = 'Delete']"));
			WaitingPageObject<StoreRegistrationPage> waiter = removalWaiter(deleteAction);
			deleteAction.click();
			acceptConfirmation();
			return waiter.get();
		}
		return this;
	}

	public StoreRegistrationEditPage editStoreRego(String storeName)
	{
		driver.findElement(
			By.xpath("//table[@id='ss_ent']/tbody/tr/td/a[text() = " + quoteXPath(storeName)
				+ "]/../../td[2]/a[text() = 'Edit']")).click();
		return new StoreRegistrationEditPage(context, false).get();
	}

}
