package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;

public class BulkPreviewPage extends AbstractPage<BulkPreviewPage>
{
	@FindBy(id = "bss_bulkDialog_okButton")
	private WebElement executeButton;
	@FindBy(id = "bss_bulkDialog_prevButton")
	private WebElement previousButton;
	@FindBy(id = "bss_bulkDialog_close")
	private WebElement closeButton;

	BulkPreviewPage(PageContext context)
	{
		super(context, By.xpath("//h3[text() = 'Preview']"));
	}

	public BulkResultsPage execute()
	{
		visibilityWaiter(executeButton).get();
		executeButton.click();
		acceptConfirmation();
		return new BulkResultsPage(context).get();
	}

	public boolean isPreviewErrored()
	{
		return isPresent(By.className("preview-error"));
	}

	public String getErrorMessage()
	{
		return driver.findElement(By.className("preview-error")).getText();
	}

	public boolean isNodePresent(String node)
	{
		return isPresent(By.xpath("//li/div[contains(text(), " + quoteXPath(node) + ")]"));
	}

	public String getNodeContents(String node)
	{
		String nodeContents = driver.findElement(By.xpath("//li/div[contains(text(), " + quoteXPath(node) + ")]"))
			.getText();
		return nodeContents.substring(nodeContents.indexOf(": ") + 2);
	}

	public <T extends PageObject> T previous(WaitingPageObject<T> targetPage)
	{
		previousButton.click();
		return targetPage.get();
	}

	public <T extends PageObject> T closeDialog(WaitingPageObject<T> targetPage)
	{
		closeButton.click();
		return targetPage.get();
	}

}
