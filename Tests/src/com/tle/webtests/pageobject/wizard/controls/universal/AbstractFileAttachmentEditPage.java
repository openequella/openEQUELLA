package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public abstract class AbstractFileAttachmentEditPage<T extends AbstractFileAttachmentEditPage<T>>
	extends
		AbstractAttachmentEditPage<T>
{
	@FindBy(id = "{wizid}_dialog_fuh_{typeId}_previewCheckBox")
	private WebElement previewCheckBox;
	@FindBy(id = "{wizid}_dialog_fuh_{typeId}_displayName")
	private WebElement nameField;
	@FindBy(id = "{wizid}_dialog_fuh_{typeId}_st")
	private WebElement thumbnailCheckbox;

	public AbstractFileAttachmentEditPage(UniversalControl universalControl)
	{
		super(universalControl);
	}

	public abstract String getTypeId();

	@Override
	protected WebElement findLoadedElement()
	{
		return getNameField();
	}

	@Override
	protected WebElement getNameField()
	{
		return nameField;
	}

	public T setPreview(boolean preview)
	{
		if( previewCheckBox.isSelected() != preview )
		{
			previewCheckBox.click();
		}
		return (T) this;
	}

	public boolean canPreview()
	{
		return isPresent(previewCheckBox);
	}

	public T setThubmnailSuppress(boolean suppressThumbnail)
	{
		if( thumbnailCheckbox.isSelected() != suppressThumbnail )
		{
			thumbnailCheckbox.click();
		}
		return (T) this;
	}

	public boolean canSuppressThumbnails()
	{
		return isPresent(thumbnailCheckbox);
	}

}
