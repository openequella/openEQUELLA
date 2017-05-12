package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class GenericAttachmentEditPage extends AbstractAttachmentEditPage<GenericAttachmentEditPage>
{
	private WebElement nameField;
	private WebElement previewCheckbox;

	public GenericAttachmentEditPage(UniversalControl universalControl, WebElement nameField, WebElement previewCheckbox)
	{
		super(universalControl);
		this.nameField = nameField;
		this.previewCheckbox = previewCheckbox;
	}

	@Override
	protected WebElement getNameField()
	{
		return nameField;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return nameField;
	}

	public GenericAttachmentEditPage setPreview(boolean preview)
	{
		if( previewCheckbox.isSelected() != preview )
		{
			previewCheckbox.click();
		}
		return this;
	}

	public boolean canPreview()
	{
		return isPresent(previewCheckbox);
	}

}
