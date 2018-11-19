package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.By;
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
	private WebElement buttonByText(String name)
	{
		return driver.findElement(By.xpath(getButtonbar()+"button[normalize-space(text())="+quoteXPath(name)+"]"));
	}

	protected WebElement getSaveButton()
	{
		return buttonByText("Save");
	}


	protected WebElement getNextButton()
	{
		return buttonByText("Next");
	}

	protected WebElement getBackToStartButton()
	{
		return buttonByText("Back to start");
	}

	@FindBy(xpath = "//img[@class='modal_close']")
	private WebElement closeButton;

	private WebElement getFooterAjax()
	{
		return byWizId("_dialogfooter");
	}

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
		return ajaxUpdate(getFooterAjax());
	}

	public WebElement byDialogXPath(String xpath)
    {
        return byWizIdIdXPath("_dialog", xpath);
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
			return getAddButton().isDisplayed();
		}
		catch( NoSuchElementException nse )
		{
			return false;
		}
	}

	protected By getAddButtonBy()
	{
		return By.xpath(getButtonbar()+"button[normalize-space(text())='Add' or normalize-space(text())='Replace']");
	}

	protected WebElement getAddButton()
	{
		return driver.findElement(getAddButtonBy());
	}
}
