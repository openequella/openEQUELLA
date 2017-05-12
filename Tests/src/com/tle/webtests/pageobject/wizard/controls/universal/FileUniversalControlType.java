package com.tle.webtests.pageobject.wizard.controls.universal;

import java.io.File;
import java.net.URL;
import java.util.Set;

import com.google.common.collect.Sets;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.PathUtils;
import com.tle.webtests.framework.EBy;
import com.tle.webtests.framework.factory.DontCache;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class FileUniversalControlType extends AbstractAttachmentDialogPage<FileUniversalControlType>
	implements
		AttachmentType<FileUniversalControlType, FileAttachmentEditPage>
{
	@FindBy(id = "{wizid}_dialog_fuh_fileUpload")
	@DontCache
	private WebElement fileUpload;
	@FindBy(id = "{wizid}_dialog_fuh_filesFromScrapbookLink")
	private WebElement addScrap;
	@FindBy(xpath = "id('{wizid}_dialog')//div[contains(@class,'fileHandler')]")
	private WebElement mainDiv;
	@FindBy(xpath = "id('{wizid}_dialog')//p[@class = 'ctrlinvalidmessage']")
	private WebElement errorMsg;
	@FindBy(id = "uploads")
	private WebElement uploadsDiv;
	@FindBy(id = "{wizid}_dialog")
	private WebElement dialog;

	public FileUniversalControlType(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return mainDiv;
	}

	@Override
	public String getType()
	{
		return "Upload a file";
	}

	public FileAttachmentEditPage uploadFile(URL url)
	{
		return upload(getPathFromUrl(url), fileEditor());
	}

	public String uploadMultiple(URL[] urls)
	{
		String[] expectedFilenames = new String[urls.length];
		Set<String> filenameSet = Sets.newHashSet();
		int i = 0;
		for( URL file : urls )
		{
			expectedFilenames[i] = uniqueFilename(filenameSet, PathUtils.getFilenameFromFilepath(file.getPath()));
			i++;
		}
		uploadMultiple(urls, expectedFilenames);
		return expectedFilenames[0];
	}

	private String uniqueFilename(Set<String> lowerFilenames, String filename)
	{
		String lowerCaseFilename = filename.toLowerCase();
		if( !lowerFilenames.contains(lowerCaseFilename) )
		{
			lowerFilenames.add(lowerCaseFilename);
			return filename;
		}

		final Pair<String, String> nameParts = PathUtils.fileParts(filename);
		final String name = nameParts.getFirst();
		final String extension = nameParts.getSecond();
		int upto = 2;
		while( true )
		{
			final String uniqueName = name + '(' + upto + ')' + (Check.isEmpty(extension) ? "" : '.' + extension);
			String uniqueLower = uniqueName.toLowerCase();
			if( !lowerFilenames.contains(uniqueLower) )
			{
				lowerFilenames.add(uniqueLower);
				return uniqueName;
			}
			upto++;
		}
	}

	public void uploadMultiple(URL[] urls, String[] expectedFilenames)
	{
		int i = 0;
		for( URL url : urls )
		{
			uploadMultiple(getPathFromUrl(url), expectedFilenames[i]);
			i++;
		}
		WebElement addButton = waitForElement(mainDiv, EBy.buttonText("Add"));
		ExpectedCondition<Boolean> disappears = removalCondition(addButton);
		addButton.click();
		waiter.until(disappears);
	}

	public FileAttachmentEditPage uploadFile(File file)
	{
		return upload(file.getAbsolutePath(), fileEditor());
	}

	private void uploadMultiple(String filename, String expectedFilename)
	{
		fileUpload.sendKeys(filename);
		WebElement uploadRow = waitForElement(uploadsDiv,
			By.xpath("div[@class='file-upload' and span/strong/text() = " + quoteXPath(expectedFilename) + "]"));
		waitForElement(uploadRow, new ByChained(By.className("progress-bar"), By.className("complete")));
	}

	public FileUniversalControlType uploadError(URL file)
	{
		fileUpload.sendKeys(getPathFromUrl(file));
		return ExpectWaiter.waiter(getErrorExpectation(), this).get();
	}

	private ExpectedCondition<?> getErrorExpectation()
	{
		// If the error is already there... wait for it to be refreshed
		if( isPresent(errorMsg) )
		{
			return ExpectedConditions2.updateOfElement(errorMsg);
		}
		return ExpectedConditions.visibilityOf(errorMsg);
	}

	public ZipAttachmentEditPage uploadZip(URL fileUrl)
	{
		return uploadZip(getPathFromUrl(fileUrl));
	}

	public FileAttachmentEditPage uploadZipAsFile(URL fileUrl)
	{
		return uploadZipAsFile(getPathFromUrl(fileUrl));
	}

	private ZipAttachmentEditPage uploadZip(String filename)
	{
		ZipOptionsPage zipOptions = upload(filename, zipOptionsPage());
		return zipOptions.unzip().selectAll();
	}

	private FileAttachmentEditPage uploadZipAsFile(String filename)
	{
		ZipOptionsPage zipOptions = upload(filename, zipOptionsPage());
		return zipOptions.normalFile();
	}

	public PackageAttachmentEditPage uploadPackage(URL url)
	{
		return upload(url, pkgEditor());
	}

	public PackageAttachmentEditPage uploadPackageOption(URL url)
	{
		PackageOptionsPage options = upload(url, new PackageOptionsPage(this));
		return options.treatAsPackage();
	}

	public PackageAttachmentEditPage uploadQTITestOption(URL url)
	{
		PackageOptionsPage options = upload(url, new PackageOptionsPage(this));
		return options.treatAsQTIQuiz();
	}

	public PackageAttachmentEditPage uploadMETSOption(URL url)
	{
		PackageOptionsPage options = upload(url, new PackageOptionsPage(this));
		return options.treatAsMETS();
	}

	public <T extends PageObject> T upload(URL fileUrl, WaitingPageObject<T> nextPage)
	{
		return upload(getPathFromUrl(fileUrl), nextPage);
	}

	public <T extends PageObject> T upload(String filename, WaitingPageObject<T> nextPage)
	{
		waitForHiddenElement(fileUpload);
		fileUpload.sendKeys(filename);

		waiter.until(ExpectedConditions2.presenceOfElement(nextButton));
		nextButton.click();
		return nextPage.get();
	}

	public FileAttachmentEditPage importFromScrapbook(String description)
	{
		addScrap.click();

		SelectionSession selectionSession = ExpectWaiter.waiter(
			ExpectedConditions2.frameToBeAvailableAndSwitchToIt(dialog, By.xpath("./div/iframe")),
			new SelectionSession(context)).get();
		new MyResourcesPage(context, "scrapbook").results().getResultForTitle(description, 1)
			.clickAction("Select", selectionSession);
		selectionSession.finishedSelecting(this);
		nextButton.click();
		return edit();
	}

	@Override
	public FileAttachmentEditPage edit()
	{
		return fileEditor().get();
	}

	public FileAttachmentEditPage fileEditor()
	{
		return new FileAttachmentEditPage(control);
	}

	public PackageAttachmentEditPage pkgEditor()
	{
		return new PackageAttachmentEditPage(control);
	}

	public ZipOptionsPage zipOptionsPage()
	{
		return new ZipOptionsPage(this);
	}

	public ZipAttachmentEditPage zipEditor()
	{
		return new ZipAttachmentEditPage(control);
	}

	public UniversalControl getControl()
	{
		return control;
	}

	public ZipAttachmentEditPage uploadZip(File file)
	{
		return uploadZip(file.getAbsolutePath());
	}

	public String getErrorMessage()
	{
		return errorMsg.getText();
	}

}
