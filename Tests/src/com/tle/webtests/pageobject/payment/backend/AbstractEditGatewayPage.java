package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.entities.AbstractEditEntityPage;

/**
 * @author Dustin
 * @param <T>
 */
public abstract class AbstractEditGatewayPage<T extends AbstractEditGatewayPage<T>>
	extends
		AbstractEditEntityPage<T, ShowGatewaysPage>
{
	@FindBy(id = "{editorSectionId}_sandboxMode")
	private WebElement sandbox;
	@FindBy(id = "{editorSectionId}_e")
	private WebElement enabled;
	@FindBy(id = "gatewayEditor")
	private WebElement ajaxDiv;

	@FindBy(className = "ctrlinvalidmessage")
	private WebElement invalidMessage;

	public AbstractEditGatewayPage(ShowGatewaysPage listPage)
	{
		super(listPage);
	}

	@Override
	protected String getEntityName()
	{
		return "payment gateway";
	}

	public void setSandboxMode(boolean input)
	{
		if( input != sandbox.isSelected() )
		{
			sandbox.click();
		}
	}

	public boolean isSandbox()
	{
		return sandbox.isSelected();
	}

	public void setEnabled(boolean input)
	{
		if( input != enabled.isSelected() )
		{
			enabled.click();
		}
	}

	public boolean isEnabled()
	{
		return enabled.isSelected();
	}

	public WaitingPageObject<T> getAjaxWaiter()
	{
		return ajaxUpdateExpect(ajaxDiv, nameField);
	}

	@Override
	public T saveWithErrors()
	{
		saveButton.click();
		return visibilityWaiter(invalidMessage).get();
	}

	@Override
	protected String getContributeSectionId()
	{
		return "gc";
	}
}
