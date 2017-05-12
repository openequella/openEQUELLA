package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * @author Dustin
 */
public class FakeGatewayEditor extends AbstractEditGatewayPage<FakeGatewayEditor>
{
	@FindBy(id = "{editorSectionId}_noDelayMode")
	private WebElement noDelayMode;

	public FakeGatewayEditor(ShowGatewaysPage listPage)
	{
		super(listPage);
	}

	public void setNoDelay(boolean input)
	{
		if( noDelayMode.isSelected() != input )
		{
			noDelayMode.click();
		}
	}

	public boolean isNoDelay()
	{
		return noDelayMode.isSelected();
	}

	@Override
	public String getEditorSectionId()
	{
		return "fge";
	}
}
