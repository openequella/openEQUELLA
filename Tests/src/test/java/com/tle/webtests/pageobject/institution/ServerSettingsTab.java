package com.tle.webtests.pageobject.institution;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;

public class ServerSettingsTab extends InstitutionTab<ServerSettingsTab>
{
	@FindBy(id = "isserver_message_message")
	private WebElement serverMessage;
	@FindBy(id = "isserver_message_enabled")
	private WebElement serverMessageEnabled;
	@FindBy(id = "isserver_message_save")
	private WebElement serverMessageSave;

	public ServerSettingsTab(PageContext context)
	{
		super(context, "Settings", "Server message");
	}

	public void setServerMessage(String message)
	{
		serverMessage.clear();
		serverMessage.sendKeys(message);
		if( Check.isEmpty(serverMessageEnabled.getAttribute("checked")) )
		{
			serverMessageEnabled.click();
		}
		serverMessageSave.click();
		get();
	}

	public void disableServerMessage()
	{
		serverMessage.clear();
		if( !Check.isEmpty(serverMessageEnabled.getAttribute("checked")) )
		{
			serverMessageEnabled.click();
		}
		serverMessageSave.click();
		get();
	}

}
