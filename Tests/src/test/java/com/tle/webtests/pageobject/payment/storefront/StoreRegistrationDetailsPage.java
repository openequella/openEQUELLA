package com.tle.webtests.pageobject.payment.storefront;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class StoreRegistrationDetailsPage extends AbstractPage<StoreRegistrationDetailsPage>
{

	public StoreRegistrationDetailsPage(PageContext context)
	{
		super(context, By.xpath("//div[@class = 'area']//h3[text() = 'Store Details']"));
	}

	public String getStoreName()
	{
		return driver.findElement(
			By.xpath("//table[@id='vsi_storeDetails']/tbody/tr/td[text() = 'Name']/following-sibling::td")).getText();
	}

	public String getURL()
	{
		return driver.findElement(By.xpath("//td[text() = 'URL']/following-sibling::td")).getText();
	}

	public String getClientId()
	{
		return driver.findElement(By.xpath("//td[text() = 'Client ID']/following-sibling::td")).getText();
	}

	// TODO: transactions allowed

	public boolean isEnabled()
	{
		if( driver.findElement(By.xpath("//td[text() = 'Enabled']/following-sibling::td")).getText().equals("Yes") )
		{
			return true;
		}
		return false;
	}

	public String getContactName()
	{
		return driver.findElement(
			By.xpath("//table[@id='vsi_contactDetails']/tbody/tr/td[text() = 'Name']/following-sibling::td")).getText();
	}

	public String getContactNumber()
	{
		return driver.findElement(By.xpath("//td[text() = 'Contact No.']/following-sibling::td")).getText();
	}

	public String getContactEmail()
	{
		return driver.findElement(By.xpath("//td[text() = 'Email']/following-sibling::td")).getText();
	}

	public boolean isFreeAllowed()
	{
		return isPresent(By.xpath("//td[text() = 'Free']"));
	}

	public boolean isOutrightAllowed()
	{
		List<WebElement> tdList = driver.findElements(By.xpath("//td[2]"));
		for( WebElement td : tdList )
		{
			if( td.getText().equals("Outright purchase") )
			{
				return true;
			}
		}
		return false;
	}

	public boolean isSubAllowed()
	{
		List<WebElement> tdList = driver.findElements(By.xpath("//td[2]"));
		for( WebElement td : tdList )
		{
			if( td.getText().equals("Subscription") )
			{
				return true;
			}
		}
		return false;
	}

}
