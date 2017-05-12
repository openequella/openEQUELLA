package com.tle.webtests.pageobject.payment.backend;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.generic.entities.AbstractShowEntitiesPage;

public class ShowStoreFrontRegistrationsPage extends AbstractShowEntitiesPage<ShowStoreFrontRegistrationsPage>
{
	public ShowStoreFrontRegistrationsPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/storefronts.do");
	}

	@Override
	protected String getSectionId()
	{
		return "ssf";
	}

	@Override
	protected String getH2Title()
	{
		return "Store front registrations";
	}

	@Override
	protected String getEmptyText()
	{
		return "There are no registered store fronts";
	}

	public EditStoreFrontRegistrationPage createRegistration()
	{
		return createEntity(new EditStoreFrontRegistrationPage(this));
	}

	public EditStoreFrontRegistrationPage editRegistration(String name)
	{
		return editEntity(new EditStoreFrontRegistrationPage(this), name);
	}

	public boolean regoExists(String name)
	{
		return entityExists(name);
	}
}
