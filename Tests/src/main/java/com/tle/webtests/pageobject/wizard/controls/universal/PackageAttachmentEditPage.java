package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class PackageAttachmentEditPage extends AbstractFileAttachmentEditPage<PackageAttachmentEditPage>
{
	@FindBy(id = "{wizid}_dialog_fuh_pd_expandButtons_1")
	private WebElement showStructure;

	public PackageAttachmentEditPage(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	public String getTypeId()
	{
		return "pd";
	}

	public PackageAttachmentEditPage showStructure()
	{
		showStructure.click();
		return this;
	}

}
