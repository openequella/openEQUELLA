package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * @author Dustin
 */
public class PaypalGatewayEditor extends AbstractEditGatewayPage<PaypalGatewayEditor>
{
	@FindBy(id = "{editorSectionId}_apiUsername")
	private WebElement username;

	public PaypalGatewayEditor(ShowGatewaysPage listPage)
	{
		super(listPage);
	}

	public void setUsername(String input)
	{
		username.clear();
		username.sendKeys(input);
	}

	public String getUsername()
	{
		return username.getAttribute("value");
	}

	@Override
	public String getEditorSectionId()
	{
		return "pge";
	}
}
