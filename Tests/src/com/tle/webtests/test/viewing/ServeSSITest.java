package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertEquals;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.AbstractSessionTest;

@TestInstitution("fiveo")
public class ServeSSITest extends AbstractSessionTest
{
	public static final String SHTML_FILE = "file/d3d0c30b-fc6d-492c-9ff4-9f6793c921d0/1/test.shtml";

	@Test
	public void serveSSI()
	{
		logon("AutoTest", "automated");
		WebDriver driver = context.getDriver();
		driver.get(context.getBaseUrl() + SHTML_FILE);
		assertEquals(driver.findElement(By.xpath("//h2")).getText(), "This is the included footer");
	}
}
