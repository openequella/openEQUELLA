package com.tle.webtests.pageobject.wizard;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.viewitem.PreviewItemDialog;
import com.tle.webtests.pageobject.viewitem.SummaryPage;

public abstract class AbstractWizardTab<T extends AbstractWizardTab<T>> extends AbstractPage<T>
{

	public AbstractWizardTab(PageContext context, By loadedBy)
	{
		super(context, loadedBy);
	}

	public AbstractWizardTab(PageContext context)
	{
		super(context);
	}

	public ConfirmationDialog save()
	{
		clickCommand("Save");
		return new ConfirmationDialog(context).get();
	}

	public SummaryPage saveNoConfirm()
	{
		clickCommand("Save");
		return new SummaryPage(context).get();
	}

	protected void clickCommand(String command)
	{
		if( command.equals("Save") )
		{
			WebElement findElement = driver.findElement(
				By.xpath("//input[contains(@class, 'action-button') and normalize-space(@value)=" + quoteXPath(command)
					+ "]"));
			// http://code.google.com/p/selenium/issues/detail?id=4175
			JavascriptExecutor js = ((JavascriptExecutor) driver);
			js.executeScript("arguments[0].scrollIntoView(false)", findElement);
			findElement.click();
		}
		else
		{
			driver.findElement(
				By.xpath("//div[@id='wizard-actions']/a[normalize-space(text())=" + quoteXPath(command) + "]")).click();
		}
	}

	public <T extends PageObject> T cancel(WaitingPageObject<T> targetPage)
	{
		clickCommand("Cancel");
		acceptConfirmation();
		return targetPage.get();
	}

	public SelectThumbnailDialog openSelectThumbnailDialog()
	{
		driver.findElement(
			By.xpath("//div[@id='more-actions']/ul/li/div/a[normalize-space(text())='Select thumbnail']")).click();
		return new SelectThumbnailDialog(context).get();
	}

	//Access Preview Window
	public PreviewItemDialog preview()
	{
		driver.findElement(By.xpath("//div[@id='more-actions']/ul/li/div/a[normalize-space(text())='Preview']"))
			.click();
		return new PreviewItemDialog(context).get();
	}
	

	public String getBreadcrumbs()
	{
		return driver.findElement(By.xpath("//div[@id='breadcrumbs']")).getText();
	}

	
}
