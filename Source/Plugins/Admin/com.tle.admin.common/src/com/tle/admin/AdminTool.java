package com.tle.admin;

import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.tle.common.applet.client.ClientService;

public abstract class AdminTool
{
	protected JFrame parentFrame;
	protected JPanel managementPanel;
	protected Driver driver;
	protected ClientService clientService;

	public abstract void toolSelected();

	public abstract void setup(Set<String> grantedPrivilges, String name);

	public void setParent(JFrame f)
	{
		parentFrame = f;
	}

	public JFrame getParentFrame()
	{
		return parentFrame;
	}

	public void setManagementPanel(JPanel p)
	{
		managementPanel = p;
	}

	public final void setDriver(Driver driver)
	{
		this.driver = driver;
		this.clientService = driver.getClientService();
	}
}
