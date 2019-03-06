package com.tle.webtests.pageobject.viewitem;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;

public class ExportItemPage extends ItemPage<ExportItemPage>
{
	@FindBy(id = "me_attachments")
	private WebElement includeFiles;
	@FindBy(xpath = "id('metsExport')//a[@title='Download']")
	private WebElement metsDownload;

	@FindBy(linkText = "Download original IMS package")
	private WebElement originalImsDownload;
	@FindBy(linkText = "Download IMS package with updated metadata")
	private WebElement updatedImsDownload;
	@FindBy(xpath = "//h2[text()='Export']")
	private WebElement exportTitle;

	public ExportItemPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return exportTitle;
	}

	public void exportAsMETS(boolean files)
	{
		if( includeFiles.isSelected() != files )
		{
			WaitingPageObject<ExportItemPage> updateWaiter = updateWaiter(metsDownload);
			includeFiles.click();
			updateWaiter.get();

		}

		driver.get(metsDownload.getAttribute("href"));
	}

	public void downloadOriginalIms()
	{
		driver.get(originalImsDownload.getAttribute("href"));
	}

	public void downloadUpdatedIms()
	{
		driver.get(updatedImsDownload.getAttribute("href"));
	}
}
