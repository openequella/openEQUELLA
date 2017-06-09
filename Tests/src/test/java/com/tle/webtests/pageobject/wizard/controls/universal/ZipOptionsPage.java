package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ZipOptionsPage extends AbstractAttachmentDialogPage<ZipOptionsPage>
{
	@FindBy(id = "{wizid}_dialog_fuh_zo_zipOptions_0")
	protected WebElement unzipOption;
	@FindBy(id = "{wizid}_dialog_fuh_zo_zipOptions_1")
	protected WebElement attachZipOption;
	private FileUniversalControlType file;

	public ZipOptionsPage(FileUniversalControlType file)
	{
		super(file.getControl());
		this.file = file;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return unzipOption;
	}

	public ZipAttachmentEditPage unzip()
	{
		unzipOption.click();
		nextButton.click();
		return file.zipEditor().get();
	}

	public FileAttachmentEditPage normalFile()
	{
		attachZipOption.click();
		nextButton.click();
		return file.fileEditor().get();
	}
}
