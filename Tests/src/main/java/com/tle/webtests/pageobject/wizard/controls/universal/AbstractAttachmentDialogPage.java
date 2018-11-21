package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.controls.NewAbstractWizardControl;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractAttachmentDialogPage<T extends AbstractAttachmentDialogPage<T>>
	extends
		NewAbstractWizardControl<T>
{
	static final String BUTTON_ADD = "Add";
	public static final String BUTTON_SAVE = "Save";
	public static final String BUTTON_NEXT = "Next";
	public static final String BUTTON_BACK = "Back to start";
	public static final String BUTTON_REPLACE = "Replace";

	protected By buttonBy(String name)
	{
		return By.xpath(getButtonbar()+"button[normalize-space(text())="+quoteXPath(name)+"]");
	}

	private WebElement buttonByText(String name)
	{
		return driver.findElement(buttonBy(name));
	}

	protected WebElement getSaveButton()
	{
		return buttonByText(BUTTON_SAVE);
	}

	protected WebElement getBackToStartButton()
	{
		return buttonByText(BUTTON_BACK);
	}

	protected WebElement getNextButton()
	{
		return buttonByText(BUTTON_NEXT);
	}

	protected WebElement getAddButton()
	{
		return buttonByText(BUTTON_ADD);
	}

	@FindBy(xpath = "//img[@class='modal_close']")
	private WebElement closeButton;

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
		return ajaxUpdate(wizIdBy("_dialogfooter"));
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
		return isVisible(buttonByText(BUTTON_ADD));
	}
}
