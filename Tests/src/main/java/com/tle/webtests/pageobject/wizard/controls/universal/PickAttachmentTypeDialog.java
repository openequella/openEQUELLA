package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class PickAttachmentTypeDialog extends AbstractAttachmentDialogPage<PickAttachmentTypeDialog>
{
	private WebElement getAttachmentTypes()
	{
		return byWizId("_dialog_pr");
	}

	public PickAttachmentTypeDialog(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getAttachmentTypes();
	}

	public <T extends AttachmentType<T, ?>> T clickType(T type)
	{
		getAttachmentTypes().findElement(By.xpath(".//h4[text()=" + quoteXPath(type.getType()) + "]")).click();
		getNextButton().click();
		return type.get();
	}
}
