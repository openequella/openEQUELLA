package com.tle.webtests.pageobject.connectors;

import org.openqa.selenium.WebElement;

public class EditEquellaConnectorPage extends AbstractConnectorEditPage<EditEquellaConnectorPage>
{
	public EditEquellaConnectorPage(ShowConnectorsPage connectorsPage)
	{
		super(connectorsPage);
	}

	@Override
	public String getEditorSectionId()
	{
		return "ece";
	}

	@Override
	public WebElement getUsernameField()
	{
		throw new Error("Not supported");
	}

	@Override
	public WebElement getTestButton()
	{
		throw new Error("Not supported");
	}

	@Override
	public String getId()
	{
		return "ece";
	}

	@Override
	public WebElement getAllowSummaryCheckbox()
	{
		throw new Error("Not supported");
	}
}
