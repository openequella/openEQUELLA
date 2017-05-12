package com.tle.webtests.pageobject.wizard.controls;


import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public abstract class AbstractWizardControl<T extends AbstractWizardControl<T>> extends NewAbstractWizardControl<T>
{
	@FindBy(xpath = "id('{wizid}')//div/p[@class=\"ctrlinvalidmessage\"]")
	private WebElement invalidMessage;

	public AbstractWizardControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	@Override
	protected abstract WebElement findLoadedElement();

	public boolean isShowingAnyError()
	{
		return invalidMessage.isDisplayed();
	}

	public String getInvalidMessage()
	{
		return (isShowingAnyError() ? invalidMessage.getText() : "");
	}
}
