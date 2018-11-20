package com.tle.webtests.pageobject.wizard.controls.universal;

import java.io.File;
import java.net.URL;
import java.util.Set;

import com.google.common.collect.Sets;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
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
	private WebElement getFileUpload()
	{
		return byWizId("_dialog_fuh_fileUpload");
	}

	private WebElement getMainDiv()
	{
		return driver.findElement(By.xpath("id('"+getWizid()+"_dialog')//div[contains(@class,'fileHandler')]"));
	}

	@FindBy(xpath = "id('uploads')/div[contains(@class, 'uploadsprogress')]")
	private WebElement uploadsDiv;

	private WebElement getDialog()
	{
		return byWizId("_dialog");
	}

	private WebElement getAddScrap()
	{
		return driver.findElement(By.id(getWizid()+"_dialog_fuh_filesFromScrapbookLink"));
	}

	public FileUniversalControlType(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getMainDiv();
	}

	@Override
	public String getType()
	{
		return "Upload a file";
	}

	public AbstractWizardControlPage<?> uploadFile(URL url)
	{
		return upload(getPathFromUrl(url), getPage());
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
		WebElement addButton = waitForElement(getMainDiv(), EBy.buttonText("Add"));
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
		getFileUpload().sendKeys(filename);
		WebElement uploadRow = waitForElement(uploadsDiv,
			By.xpath("div[@class='file-upload' and span/strong/text() = " + quoteXPath(expectedFilename) + "]"));
		waitForElement(uploadRow, new ByChained(By.className("progress-bar"), By.className("complete")));
	}

	public FileUniversalControlType uploadError(URL file, String errorMessage)
	{
		ExpectedCondition<?> errorExpectation = getErrorExpectation(errorMessage);
		getFileUpload().sendKeys(getPathFromUrl(file));
		return ExpectWaiter.waiter(errorExpectation, this).get();
	}

	private ExpectedCondition<?> getErrorExpectation(String msg)
	{
		String xpath = "id('"+getWizid()+"_dialog')//p[@class = 'ctrlinvalidmessage' and text() = "+quoteXPath(msg)+"]";
		return ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath));
	}

	public FileAttachmentEditPage uploadZip(URL fileUrl)
	{
		return uploadZip(getPathFromUrl(fileUrl));
	}

	public FileAttachmentEditPage uploadZipAsFile(URL fileUrl)
	{
		return uploadZipAsFile(getPathFromUrl(fileUrl));
	}

	private FileAttachmentEditPage uploadZip(String filename)
	{
		FileAttachmentEditPage upload = upload(filename, fileEditor());
		return upload.unzip().selectAll();
	}

	private FileAttachmentEditPage uploadZipAsFile(String filename)
	{
		return upload(filename, fileEditor());
	}

	public <T extends PageObject> T uploadPackage(URL url, WaitingPageObject<T> backTo)
	{
		return upload(url, backTo);
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
		waitForHiddenElement(getFileUpload());
		getFileUpload().sendKeys(filename);

		waiter.until(ExpectedConditions.presenceOfElementLocated(getAddButtonBy()));
		getAddButton().click();
		return nextPage.get();
	}

	public FileAttachmentEditPage importFromScrapbook(String description)
	{
		getAddScrap().click();

		SelectionSession selectionSession = ExpectWaiter.waiter(
			ExpectedConditions2.frameToBeAvailableAndSwitchToIt(getDialog(), By.xpath("./div/iframe")),
			new SelectionSession(context)).get();
		new MyResourcesPage(context, "scrapbook").results().getResultForTitle(description, 1)
			.clickAction("Select", selectionSession);
		selectionSession.finishedSelecting(this);
		getNextButton().click();
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

	public UniversalControl getControl()
	{
		return control;
	}

	public FileAttachmentEditPage uploadZip(File file)
	{
		return uploadZip(file.getAbsolutePath());
	}

}
