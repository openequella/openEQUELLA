package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

/**
 * @author Dustin
 */
public class EditGatewayPage extends AbstractEditGatewayPage<EditGatewayPage>
{
	public static final String CREATE_GATEWAY_TITLE = "Create payment gateway";
	public static final String EDIT_GATEWAY_TITLE = "Edit payment gateway";

	@FindBy(id = "{contributeSectionId}_gt")
	private WebElement type;

	public EditGatewayPage(ShowGatewaysPage listPage)
	{
		super(listPage);
	}

	public PaypalGatewayEditor setTypetoPaypal()
	{
		PaypalGatewayEditor editor = new PaypalGatewayEditor(getShowListPage()).setCreating(true);
		WaitingPageObject<PaypalGatewayEditor> waiter = editor.getAjaxWaiter();
		new EquellaSelect(context, type).selectByValue("paypal");
		return waiter.get();
	}

	public FakeGatewayEditor setTypetoFake()
	{
		FakeGatewayEditor editor = new FakeGatewayEditor(getShowListPage()).setCreating(true);
		WaitingPageObject<FakeGatewayEditor> waiter = editor.getAjaxWaiter();
		new EquellaSelect(context, type).selectByValue("fake");
		return waiter.get();
	}

	@Override
	protected String getEditorSectionId()
	{
		return "";
	}
}
