package com.tle.admin.itemdefinition;

import javax.swing.JPanel;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.applet.client.ClientService;

public abstract class AbstractExtensionConfigPanel extends JPanel
{
	protected ClientService clientService;

	public void setClientService(ClientService clientService)
	{
		this.clientService = clientService;
	}

	public abstract void load(String stagingId, ItemDefinition itemdef);

	public abstract void save(ItemDefinition itemdef);
}