package com.tle.webtests.framework;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.webtests.pageobject.AbstractPage;

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
