/*
 * Created on 11/11/2005
 */
package com.tle.admin.baseentity;

import java.awt.Component;

import javax.swing.JDialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.gui.JStatusBar;
import com.tle.admin.Driver;
import com.tle.admin.PluginServiceImpl;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.JFakePanel;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.adminconsole.RemoteAdminService;
import com.tle.common.applet.client.ClientService;

public abstract class BaseEntityTab<T extends BaseEntity> extends JFakePanel
{
	public static final Log LOGGER = LogFactory.getLog(BaseEntityTab.class);

	protected EditorState<T> state;

	protected ClientService clientService;
	protected PluginServiceImpl pluginService;
	protected RemoteAdminService adminService;

	protected JStatusBar statusBar;
	protected JDialog parent;
	protected DynamicTabService dynamicTabService;

	public void setDriver(Driver driver)
	{
		this.clientService = driver.getClientService();
		this.pluginService = driver.getPluginService();

		this.adminService = clientService.getService(RemoteAdminService.class);
	}

	public void setParent(JDialog parent)
	{
		this.parent = parent;
	}

	public void setStatusBar(JStatusBar statusBar)
	{
		this.statusBar = statusBar;
	}

	public void setState(EditorState<T> state)
	{
		this.state = state;
	}

	public void setDynamicTabService(DynamicTabService dynamicTabService)
	{
		this.dynamicTabService = dynamicTabService;
	}

	public abstract void save();

	public void afterSave()
	{
		// Nothing by default
	}

	public abstract void load();

	public abstract void init(Component parent);

	/**
	 * @return the title that should appear on the tab.
	 */
	public abstract String getTitle();

	/**
	 * The tab must validate it's data.
	 * 
	 * @throws EditorException if something is invalid.
	 */
	public abstract void validation() throws EditorException;
}
