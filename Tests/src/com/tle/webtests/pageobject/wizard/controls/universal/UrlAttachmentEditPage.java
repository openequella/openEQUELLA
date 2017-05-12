package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class UrlAttachmentEditPage extends AbstractAttachmentEditPage<UrlAttachmentEditPage>
{
	@FindBy(id = "{wizid}_dialog_uh_displayName")
	protected WebElement nameField;
	@FindBy(id = "{wizid}_dialog_uh_previewCheckBox")
	protected WebElement previewCheckBox;

	public UrlAttachmentEditPage(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return nameField;
	}

	@Override
	protected WebElement getNameField()
	{
		return nameField;
	}

	public UrlAttachmentEditPage setPreview(boolean preview)
	{
		if( previewCheckBox.isSelected() != preview )
		{
			previewCheckBox.click();
		}
		return this;
	}

	public boolean canPreview()
	{
		return isPresent(previewCheckBox);
	}
}
