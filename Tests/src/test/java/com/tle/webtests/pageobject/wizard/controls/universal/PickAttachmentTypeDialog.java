package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class PickAttachmentTypeDialog extends AbstractAttachmentDialogPage<PickAttachmentTypeDialog>
{
	@FindBy(id = "{wizid}_dialog_pr")
	private WebElement attachmentTypes;

	public PickAttachmentTypeDialog(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return attachmentTypes;
	}

	public <T extends AttachmentType<T, ?>> T clickType(T type)
	{
		attachmentTypes.findElement(By.xpath(".//h4[text()=" + quoteXPath(type.getType()) + "]")).click();
		nextButton.click();
		return type.get();
	}
}
