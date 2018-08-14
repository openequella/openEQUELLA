package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public abstract class AbstractUniversalControlType<T extends AbstractUniversalControlType<T>>
	extends
		AbstractAttachmentDialogPage<T> implements AttachmentType<T, GenericAttachmentEditPage>
{

	public AbstractUniversalControlType(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getFindElement();
	}

	abstract public WebElement getFindElement();

	abstract public WebElement getNameField();

	public PickAttachmentTypeDialog backToStart()
	{
		backToStartButton.click();
		return new PickAttachmentTypeDialog(control).get();
	}

	@Override
	public GenericAttachmentEditPage edit()
	{
		return editPage().get();
	}

	public GenericAttachmentEditPage editPage()
	{
		return new GenericAttachmentEditPage(control)
		{

			@Override
			protected WebElement getNameField()
			{
				return AbstractUniversalControlType.this.getNameField();
			}

			@Override
			protected WebElement getPreviewCheckbox()
			{
				return AbstractUniversalControlType.this.getPreviewCheckbox();
			}
		};
	}

	protected WebElement getPreviewCheckbox()
	{
		return null;
	}
}
