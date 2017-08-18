package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FileAttachmentEditPage extends AbstractFileAttachmentEditPage<FileAttachmentEditPage>
{
	@FindBy(id = "{wizid}_dialog_fuh_fd_selectAll")
	private WebElement selectAll;
	@FindBy(id = "{wizid}_dialog_fuh_fd_fileListDiv")
	private WebElement zipList;
	@FindBy(id = "{wizid}_dialog_fuh_fd_executeUnzip")
	private WebElement executeUnzip;
	@FindBy(id = "{wizid}_dialog_fuh_fd_attachZip")
	private WebElement attachZipFile;

	public FileAttachmentEditPage(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	public String getTypeId()
	{
		return "fd";
	}

	public FileAttachmentEditPage selectAll()
	{
		selectAll.click();
		return this;
	}

	public FileAttachmentEditPage select(int index)
	{
		driver.findElement(By.id("s" + index)).click();
		return this;
	}

	public FileAttachmentEditPage unzip()
	{
		executeUnzip.click();
		waitForElement(zipList);
		return this;
	}

	public FileAttachmentEditPage setAttachZip(boolean attachZip)
	{
		if (attachZipFile.isSelected() != attachZip)
		{
			attachZipFile.click();
		}
		return this;
	}

	public UniversalControl save(boolean hidden)
	{
		return super.save(getName(), hidden, false);
	}
}
