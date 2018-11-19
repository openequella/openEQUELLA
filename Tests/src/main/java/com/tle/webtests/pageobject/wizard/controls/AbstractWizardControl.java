package com.tle.webtests.pageobject.wizard.controls;


import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public abstract class AbstractWizardControl<T extends AbstractWizardControl<T>> extends NewAbstractWizardControl<T>
{
	private WebElement getInvalidMessageWE()
	{
		return byWizIdXPath("//div/p[@class='ctrlinvalidmessage']");
	}

	public AbstractWizardControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	@Override
	protected abstract WebElement findLoadedElement();

	public boolean isShowingAnyError()
	{
		return getInvalidMessageWE().isDisplayed();
	}

	public String getInvalidMessage()
	{
		return (isShowingAnyError() ? getInvalidMessageWE().getText() : "");
	}
}
