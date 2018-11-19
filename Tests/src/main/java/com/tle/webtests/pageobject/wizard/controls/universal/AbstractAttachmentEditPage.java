package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;

import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public abstract class AbstractAttachmentEditPage<T extends AbstractAttachmentDialogPage<T>>
	extends
		AbstractAttachmentDialogPage<T> implements AttachmentEditPage
{
	protected UniversalControl control;

	protected AbstractAttachmentEditPage(UniversalControl universalControl)
	{
		super(universalControl);
		this.control = universalControl;
	}

	@Override
	public String getName()
	{
		return getNameField().getAttribute("value");
	}

	public void setName(String name)
	{
		getNameField().clear();
		getNameField().sendKeys(name);
	}

	protected abstract WebElement getNameField();

	@Override
	public UniversalControl save()
	{
		String newName = getName();
		return save(newName, false, false);
	}

	/**
	 * 
	 * @param newName
	 * @param disabled Will the attachment show as "hidden from summary"?
	 * @return
	 */
	protected UniversalControl save(String newName, boolean hidden, boolean disabled)
	{
		if( hidden )
		{
			newName += " (hidden from summary view)";
		}
		// If there is an attachment with the same name already there then we
		// need to wait for it to go stale (ie gone)
		WaitingPageObject<UniversalControl> waiter;
		if( control.hasResource(newName) )
		{
			waiter = control.attachGoneWaiter(newName);
		}
		else
		{
			waiter = control.attachNameWaiter(newName, disabled);
		}
		getSaveButton().click();
		return waiter.get();
	}

	@SuppressWarnings("unchecked")
	public T setDisplayName(String newName)
	{
		WebElement nameField = getNameField();
		nameField.clear();
		nameField.sendKeys(newName);
		return (T) this;
	}

	public PickAttachmentTypeDialog backToStart()
	{
		getBackToStartButton().click();
		return new PickAttachmentTypeDialog(control).get();
	}

}
