package com.tle.webtests.framework;

import com.google.common.collect.Maps;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class StandardDriverPool
{
	private final Map<WebDriverCheckout, WebDriverCheckout> checkoutDrivers = Maps.newIdentityHashMap();
	private final Map<WebDriverCheckout, WebDriverCheckout> returnedDrivers = Maps.newIdentityHashMap();
	private final StandardDriverFactory factory;
	private static ChromeDriverService service;

	public StandardDriverPool(TestConfig config) throws IOException
	{
		this.factory = new StandardDriverFactory(config);
	}

	public synchronized void shutdown()
	{
		for( WebDriverCheckout driver : returnedDrivers.keySet() )
		{
			driver.getDriver().quit();
		}
		for( WebDriverCheckout driver : checkoutDrivers.keySet() )
		{
			driver.getDriver().quit();
		}
	}

	public synchronized WebDriverCheckout getDriver(WebDriverCheckout preferredDriver, Class<?> clazz) throws Exception
	{
		WebDriverCheckout checkout;
		if( returnedDrivers.containsKey(preferredDriver) )
		{
			checkout = returnedDrivers.remove(preferredDriver);
		}
		else if( !returnedDrivers.isEmpty() )
		{
			Iterator<WebDriverCheckout> iter = returnedDrivers.values().iterator();
			checkout = new WebDriverCheckout(iter.next().getDriver());
			iter.remove();
		}
		else
		{
			WebDriver driver = factory.getDriver(clazz);
			checkout = new WebDriverCheckout(driver);
		}
		checkoutDrivers.put(checkout, checkout);
		return checkout;
	}

	public synchronized void releaseDriver(WebDriverCheckout returned)
	{
		assert returned != null;
		checkoutDrivers.remove(returned);
		try
		{
			returned.getDriver().getCurrentUrl();
		}
		catch( Exception e )
		{
			System.out.println("Quitting unreachable driver: " + e);
			try
			{
				returned.getDriver().quit();
			}
			catch( Exception ex )
			{
				System.out.println("ERROR couldn't quit driver: " + ex);
			}
			return;
		}

		returnedDrivers.put(returned, returned);
	}
}
