package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.PopupTermDialog;
import com.tle.webtests.pageobject.generic.component.StringSelectedStuff;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class PopupTermControl extends AbstractWizardControl<PopupTermControl>
{
	@FindBy(id = "{wizid}d_addTermLink")
	private WebElement addTermButton;
	@FindBy(id = "{wizid}dpopupbrowserControl")
	private WebElement selectionsElem;

	public PopupTermControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return addTermButton;
	}

	protected WebElement getControlElement()
	{
		return selectionsElem;
	}

	public PopupTermDialog openDialog()
	{
		addTermButton.click();
		return new PopupTermDialog(context, page.getControlId(ctrlnum) + "d_popupBrowserDialog", page, ctrlnum).get();
	}

	public WaitingPageObject<StringSelectedStuff> selectWaiter(String newSelection)
	{
		return getSelections().selectionWaiter(newSelection);
	}

	public StringSelectedStuff getSelections()
	{
		return new StringSelectedStuff(context, getControlElement());
	}
}
