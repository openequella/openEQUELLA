package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class ZipAttachmentEditPage extends AbstractFileAttachmentEditPage<ZipAttachmentEditPage>
{
	@FindBy(id = "{wizid}_dialog_fuh_zd_attachZip")
	private WebElement attachZipFile;
	@FindBy(id = "{wizid}_dialog_fuh_zd_selectAll")
	private WebElement selectAll;
	@FindBy(id = "{wizid}_dialog_fuh_zd_fileListDiv")
	private WebElement zipList;
	@FindBy(id = "{wizid}_dialog_fuh_zd_displayName")
	private WebElement nameField;

	public ZipAttachmentEditPage(UniversalControl universalControl)
	{
		super(universalControl);
	}

	public ZipAttachmentEditPage setAttachZip(boolean attachZip)
	{
		if( attachZipFile.isSelected() != attachZip )
		{
			attachZipFile.click();
		}
		return this;
	}

	public ZipAttachmentEditPage selectAll()
	{
		selectAll.click();
		return this;
	}

	public ZipAttachmentEditPage select(int index)
	{
		driver.findElement(By.id("s" + index)).click();
		return this;
	}

	@Override
	public UniversalControl save()
	{
		String name = getName();
		if( !attachZipFile.isSelected() )
		{
			return save(name, true);
		}
		return save(name, false);
	}

	@Override
	public String getTypeId()
	{
		return "zd";
	}

}
