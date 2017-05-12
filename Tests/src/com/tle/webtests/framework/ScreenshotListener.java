package com.tle.webtests.framework;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.Utils;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.webtests.test.AbstractTest;

public class ScreenshotListener implements IInvokedMethodListener
{

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult)
	{
		// nothing
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult)
	{
		if( testResult.getStatus() != ITestResult.FAILURE )
		{
			return;
		}
		Reporter.setCurrentTestResult(testResult);
		try
		{
			Throwable throwable = testResult.getThrowable();
			throwable.printStackTrace();
			Reporter.log("<h3>" + testResult.getTestClass().getName() + "." + testResult.getName() + "</h3>");
			String testName = testResult.getTestName();
			if( !Check.isEmpty(testName) )
			{
				Reporter.log("<h2>" + testName + "</h2>");
			}

			Object[] parameters = testResult.getParameters();
			if( parameters != null && parameters.length > 0 )
			{
				StringBuilder sb = new StringBuilder();
				sb.append("<br>Parameters: ");
				for( int j = 0; j < parameters.length; j++ )
				{
					if( j > 0 )
					{
						sb.append(", ");
					}
					sb.append(parameters[j] == null ? "null" : parameters[j].toString());
				}
				Reporter.log(sb.toString());
			}

			Object testInstance = testResult.getMethod().getInstance();
			if( testInstance instanceof AbstractTest )
			{
				AbstractTest test = (AbstractTest) testInstance;
				TestInstitution institution = test.getClass().getAnnotation(TestInstitution.class);
				if( institution != null )
				{
					Reporter.log("<strong>Institution:</strong> " + institution.value());
				}
				String alertText = null;
				TestConfig config = test.getTestConfig();
				WebDriver driver = test.getContext().getCurrentDriver();
				if( driver != null )
				{
					if( throwable instanceof UnhandledAlertException )
					{
						alertText = ((UnhandledAlertException) throwable).getAlertText();

						// Cant take a screenshot if there is still an alert
						try
						{
							// Have to get the real alert to dismiss it
							Alert realAlert = driver.switchTo().alert();
							alertText = realAlert.getText();
							realAlert.dismiss();
						}
						catch( NoAlertPresentException nape )
						{
							// nothing
						}
					}

					String screenshot = takeScreenshot(driver, config.getScreenshotFolder(), testResult,
						config.isChromeDriverSet());
					String error = captureErrorPage(driver, testResult);
					String jsError = captureJavascriptError(driver, testResult);
					test.invalidateSession();

					Reporter.log("<strong>Failed at url:</strong> " + driver.getCurrentUrl());
					if( !Check.isEmpty(error) )
					{
						Reporter.log("<strong>Captured error: </strong><pre>" + error + "</pre>");
					}
					if( !Check.isEmpty(screenshot) )
					{
						Reporter.log(String.format("<strong>Screenshot:</strong> <a href='%s/%s'>%s</a>",
							"screenshots", screenshot, screenshot));
					}

					if( !Check.isEmpty(jsError) )
					{
						Reporter.log("<strong>JS error: </strong><pre>" + jsError + "</pre>");
					}

					Reporter.log("<strong>Message: </strong>" + StringEscapeUtils.escapeHtml4(throwable.getMessage()));

					if( alertText != null )
					{
						System.err.println("Alert text:" + alertText);
						Reporter.log("<strong>Alert Text: </strong>" + StringEscapeUtils.escapeHtml4(alertText));
					}

					Throwable tw = testResult.getThrowable();
					String stackTrace = "";
					String fullStackTrace = "";

					String id = "stack-trace" + testResult.hashCode();
					StringBuffer sb = new StringBuffer();

					if( null != tw )
					{
						String[] stackTraces = Utils.stackTrace(tw, true);
						fullStackTrace = stackTraces[1];
						stackTrace = "<div><pre>" + stackTraces[0] + "</pre></div>";

						sb.append(stackTrace);
						// JavaScript link
						sb.append(
							"<a href='#' onClick=\"document.getElementById('" + id
								+ "').style.display='block';\">Show full stacktrace</a>")
							.append("<div class='stack-trace' id='" + id + "' style='display: none'>")
							.append("<pre>" + fullStackTrace + "</pre>").append("</div>");
						Reporter.log(sb.toString());
					}
				}
			}
		}
		catch( Throwable t )
		{
			System.err.println("Error capturing screenshot");
			t.printStackTrace();
		}
	}

	public static String takeScreenshot(WebDriver driver, File screenshotFolder, ITestResult testResult, boolean chrome)
	{
		ITestNGMethod method = testResult.getMethod();
		String filename = testResult.getTestClass().getName() + "_" + method.getMethodName();
		return takeScreenshot(driver, screenshotFolder, filename, chrome);
	}

	// See https://code.google.com/p/chromedriver/issues/detail?id=294
	private static File chromeScreenie(WebDriver driver)
	{
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");

		int totalWidth = ((Long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollWidth"))
			.intValue();
		int totalHeight = ((Long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight"))
			.intValue();

		// Get the Size of the Viewport
		int viewportWidth = ((Long) ((JavascriptExecutor) driver)
			.executeScript("return document.documentElement.clientWidth")).intValue();
		int viewportHeight = ((Long) ((JavascriptExecutor) driver)
			.executeScript("return document.documentElement.clientHeight")).intValue();

		// Split the Screen in multiple Rectangles
		List<Rectangle> rectangles = Lists.newArrayList();
		// Loop until the Total Height is reached
		for( int i = 0; i < totalHeight; i += viewportHeight )
		{
			int newHeight = viewportHeight;
			// Fix if the Height of the Element is too big
			if( i + viewportHeight > totalHeight )
			{
				newHeight = totalHeight - i;
			}
			// Loop until the Total Width is reached
			for( int ii = 0; ii < totalWidth; ii += viewportWidth )
			{
				int newWidth = viewportWidth;
				// Fix if the Width of the Element is too big
				if( ii + viewportWidth > totalWidth )
				{
					newWidth = totalWidth - ii;
				}

				// Create and add the Rectangle
				Rectangle currRect = new Rectangle(ii, i, newWidth, newHeight);
				rectangles.add(currRect);
			}
		}

		// Build the Image
		BufferedImage stitchedImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_3BYTE_BGR);
		// Get all Screenshots and stitch them together
		Rectangle pr = null;
		for( Rectangle cr : rectangles )
		{
			// Calculate the Scrolling (if needed)
			if( pr != null )
			{
				int xDiff = (cr.x + cr.width) - (pr.x + pr.width);
				int yDiff = (cr.y + cr.height) - (pr.y + pr.height);
				// Scroll
				((JavascriptExecutor) driver).executeScript(MessageFormat.format("window.scrollBy({0}, {1})", xDiff,
					yDiff));
				try
				{
					Thread.sleep(200);
				}
				catch( InterruptedException e )
				{
					// Don't care
				}
			}

			// Take Screenshot
			// byte[] screenshotBytes = ((TakesScreenshot)
			// driver).getScreenshotAs(OutputType.BYTES);
			File blah = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

			// Build an Image out of the Screenshot
			Image screenshotImage;
			try
			{
				screenshotImage = ImageIO.read(blah);
				// Calculate the Source Rectangle
				Rectangle sr = new Rectangle(viewportWidth - cr.width, viewportHeight - cr.height, cr.width, cr.height);

				// Copy the Image
				// stitchedImage = new BufferedImage(totalWidth, totalHeight,
				// BufferedImage.TYPE_3BYTE_BGR);
				Graphics g = stitchedImage.createGraphics();
				// g.drawImage(screenshotImage, cr.x, cr.y, sr.x, sr.y, null);
				// System.out.println("Rectangle " + cr);
				// System.out.println("Source: " + sr);
				// System.out.println(MessageFormat.format("cr  x:{0} y:{1} x2:{2} y2:{3}, sr x:{4} y:{5} x2:{6} y2:{7}",
				// cr.x, cr.y, cr.x + cr.width, cr.y + cr.height, sr.x, sr.y,
				// sr.x + sr.width, sr.y + sr.height));
				g.drawImage(screenshotImage, cr.x, cr.y, cr.x + cr.width, cr.y + cr.height, sr.x, sr.y,
					sr.x + sr.width, sr.y + sr.height, null);

				// ImageIO.write(stitchedImage, "png",
				// File.createTempFile("screenshot", ".png"));

				// Set the Previous Rectangle
				pr = cr;
			}
			catch( IOException e )
			{
				// blah
			}
		}
		try
		{
			File tmpFile = File.createTempFile("screenshot", ".png");
			tmpFile.deleteOnExit();

			ImageIO.write(stitchedImage, "png", tmpFile);
			return tmpFile;
		}
		catch( IOException e )
		{
			// Boring
		}

		// Fail
		return null;
	}

	public static String takeScreenshot(WebDriver driver, File screenshotFolder, String filename, boolean chrome)
	{
		File screenshot = null;
		if( chrome )
		{
			screenshot = chromeScreenie(driver);
		}
		else
		{
			screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		}

		if( !screenshotFolder.exists() )
		{
			screenshotFolder.mkdirs();
		}

		File destScreenshot = new File(screenshotFolder, filename + ".png");
		int count = 2;
		while( destScreenshot.exists() )
		{
			destScreenshot = new File(screenshotFolder, filename + '(' + (count++) + ").png");
		}
		if( screenshot != null && !screenshot.renameTo(destScreenshot) )
		{
			boolean failed = false;
			try
			{
				FileUtils.copyFile(screenshot, destScreenshot);
				screenshot.delete();
				failed = !destScreenshot.exists();

			}
			catch( IOException e )
			{
				failed = true;
			}

			if( failed )
			{
				System.err.println("Failed to rename " + screenshot + " screenshot to " + destScreenshot);
				return null;
			}
		}

		System.err.println("Failure screenshot:" + destScreenshot);
		return destScreenshot.getName();
	}

	private String captureErrorPage(WebDriver driver, ITestResult testResult)
	{
		WebElement errorDiv = null;
		try
		{
			errorDiv = driver.findElement(By.xpath("//div[contains(@class,'area') and contains(@class,'error')]"));

		}
		catch( NotFoundException e1 )
		{
			try
			{
				errorDiv = driver.findElement(By.xpath("//p[contains(@class,'warning')][2]"));

			}
			catch( NotFoundException e2 )
			{

			}
		}

		if( errorDiv != null )
		{
			EquellaErrorPageException exception = new EquellaErrorPageException(errorDiv.getText(),
				testResult.getThrowable());
			testResult.setThrowable(exception);
			return errorDiv.getText();
		}
		return null;
	}

	private String captureJavascriptError(WebDriver driver, ITestResult testResult)
	{
		try
		{
			driver.findElement(By.xpath("//body[@jserror-msg]"));

		}
		catch( NotFoundException e1 )
		{
			return null;
		}

		WebElement body = driver.findElement(By.tagName("body"));
		String error = body.getAttribute("jserror-msg");
		error += " - " + body.getAttribute("jserror-url");
		error += ":" + body.getAttribute("jserror-line");
		return error;
	}
}
