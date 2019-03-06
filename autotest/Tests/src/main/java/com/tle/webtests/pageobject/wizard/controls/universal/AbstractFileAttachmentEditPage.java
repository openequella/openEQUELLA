package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public abstract class AbstractFileAttachmentEditPage<T extends AbstractFileAttachmentEditPage<T>>
	extends
		AbstractAttachmentEditPage<T>
{
	private WebElement getPreviewCheckBox()
	{
		return byWizId("_dialog_fuh_"+getTypeId()+"_previewCheckBox");
	}

	private WebElement getThumbnailCheckbox()
	{
		return byWizId("_dialog_fuh_"+getTypeId()+"_st");
	}


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
		return driver.findElement(By.id(getWizid()+"_dialog_fuh_"+getTypeId()+"_displayName"));
	}

	public T setPreview(boolean preview)
	{
		if( getPreviewCheckBox().isSelected() != preview )
		{
		    getPreviewCheckBox().click();
		}
		return (T) this;
	}

	public boolean canPreview()
	{
		return isPresent(getPreviewCheckBox());
	}

	public T setThubmnailSuppress(boolean suppressThumbnail)
	{
		if( getThumbnailCheckbox().isSelected() != suppressThumbnail )
		{
            getThumbnailCheckbox().click();
		}
		return (T) this;
	}

	public boolean canSuppressThumbnails()
	{
		return isPresent(getThumbnailCheckbox());
	}

}
