package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class UrlAttachmentEditPage extends AbstractAttachmentEditPage<UrlAttachmentEditPage>
{
	protected WebElement getNameField()
	{
		return byWizId("_dialog_uh_displayName");
	}

	protected WebElement getPreviewCheckBox()
	{
		return byWizId("_dialog_uh_previewCheckBox");
	}

	public UrlAttachmentEditPage(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getNameField();
	}


	public UrlAttachmentEditPage setPreview(boolean preview)
	{
		if( getPreviewCheckBox().isSelected() != preview )
		{
			getPreviewCheckBox().click();
		}
		return this;
	}

	public boolean canPreview()
	{
		return isPresent(getPreviewCheckBox());
	}
}
