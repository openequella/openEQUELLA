package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public abstract class GenericAttachmentEditPage extends AbstractAttachmentEditPage<GenericAttachmentEditPage>
{

	protected GenericAttachmentEditPage(UniversalControl universalControl)
	{
		super(universalControl);
	}

	protected abstract WebElement getNameField();
	protected abstract WebElement getPreviewCheckbox();

	@Override
	protected WebElement findLoadedElement()
	{
		return getNameField();
	}

	public GenericAttachmentEditPage setPreview(boolean preview)
	{
		WebElement previewCheckbox = getPreviewCheckbox();
		if( previewCheckbox.isSelected() != preview )
		{
			previewCheckbox.click();
		}
		return this;
	}

	public boolean canPreview()
	{
		return isPresent(getPreviewCheckbox());
	}

}
