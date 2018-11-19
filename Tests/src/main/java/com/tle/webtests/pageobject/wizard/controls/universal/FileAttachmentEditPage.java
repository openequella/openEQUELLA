package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FileAttachmentEditPage extends AbstractFileAttachmentEditPage<FileAttachmentEditPage>
{
	private WebElement getSelectAll()
	{
		return byWizId("_dialog_fuh_fd_selectAll");
	}

	private WebElement getZipList()
	{
		return byWizId("_dialog_fuh_fd_fileListDiv");
	}

	private WebElement getExecuteUnzip()
	{
		return byWizId("_dialog_fuh_fd_executeUnzip");
	}

	private WebElement getAttachZipFile()
	{
		return byWizId("_dialog_fuh_fd_attachZip");
	}


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
		getSelectAll().click();
		return this;
	}

	public FileAttachmentEditPage select(int index)
	{
		driver.findElement(By.id("s" + index)).click();
		return this;
	}

	public FileAttachmentEditPage unzip()
	{
		getExecuteUnzip().click();
		waitForElement(getZipList());
		return this;
	}

	public FileAttachmentEditPage setAttachZip(boolean attachZip)
	{
		if (getAttachZipFile().isSelected() != attachZip)
		{
			getAttachZipFile().click();
		}
		return this;
	}

	public UniversalControl save(boolean hidden)
	{
		return super.save(getName(), hidden, false);
	}
}
