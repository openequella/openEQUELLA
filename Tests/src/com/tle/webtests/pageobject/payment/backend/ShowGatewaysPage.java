package com.tle.webtests.pageobject.payment.backend;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.entities.AbstractShowEntitiesPage;

/**
 * @author Dustin
 */
public class ShowGatewaysPage extends AbstractShowEntitiesPage<ShowGatewaysPage>
{
	public static final String SHOW_GATEWAYS_TITLE = "Payment gateways";
	public static final String EMPTY_TEXT = "There are no editable payment gateways";

	public ShowGatewaysPage(PageContext context)
	{
		super(context);
	}

	public EditGatewayPage createNewGateway()
	{
		return createEntity(new EditGatewayPage(this));
	}

	public PaypalGatewayEditor editPaypalGateway(PrefixedName title)
	{
		return editEntity(new PaypalGatewayEditor(this), title);
	}

	public FakeGatewayEditor editFakeGateway(PrefixedName title)
	{
		return editEntity(new FakeGatewayEditor(this), title);
	}

	// The default behaviour is not good enough. (Enable is not available if you
	// have a gateway of the same type already enabled)
	@Override
	public boolean isEntityDisabled(PrefixedName name)
	{
		return actionExists(name, "Delete");
	}

	@Override
	protected String getSectionId()
	{
		return "sg";
	}

	@Override
	protected String getH2Title()
	{
		return SHOW_GATEWAYS_TITLE;
	}

	@Override
	protected String getEmptyText()
	{
		return EMPTY_TEXT;
	}
}
