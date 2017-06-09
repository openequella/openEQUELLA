package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.controls.NewAbstractWizardControl;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public abstract class AbstractAttachmentDialogPage<T extends AbstractAttachmentDialogPage<T>>
	extends
		NewAbstractWizardControl<T>
{
	@FindBy(xpath = "{buttonbar}button[normalize-space(text())='Save']")
	protected WebElement saveButton;
	@FindBy(xpath = "{buttonbar}button[normalize-space(text())='Next']")
	protected WebElement nextButton;
	@FindBy(xpath = "{buttonbar}button[normalize-space(text())='Add' or normalize-space(text())='Replace']")
	protected WebElement addButton;
	@FindBy(xpath = "{buttonbar}button[normalize-space(text())='Back to start']")
	protected WebElement backToStartButton;
	@FindBy(xpath = "//img[@class='modal_close']")
	private WebElement closeButton;
	@FindBy(id = "{wizid}_dialogfooter")
	private WebElement footerAjax;

	protected UniversalControl control;

	public AbstractAttachmentDialogPage(UniversalControl universalControl)
	{
		super(universalControl.getContext(), universalControl.getCtrlNum(), universalControl.getPage());
		this.control = universalControl;
	}

	@Override
	protected abstract WebElement findLoadedElement();

	public WaitingPageObject<T> submitWaiter()
	{
		return ajaxUpdate(footerAjax);
	}

	public String getButtonbar()
	{
		return "id('" + getWizid() + "_dialog')//div[@class='modal-footer-inner']/";
	}

	public UniversalControl close()
	{
		WaitingPageObject<UniversalControl> waiter = control.removalWaiter(closeButton);
		closeButton.click();
		return waiter.get();
	}

	public boolean canAdd()
	{
		try
		{
			return addButton.isDisplayed();
		}
		catch( NoSuchElementException nse )
		{
			return false;
		}
	}
}
