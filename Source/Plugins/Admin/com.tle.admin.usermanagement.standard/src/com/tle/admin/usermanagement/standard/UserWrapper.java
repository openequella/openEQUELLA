/*
 * Created on Mar 21, 2005
 */
package com.tle.admin.usermanagement.standard;

import com.tle.admin.gui.EditorException;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.admin.usermanagement.internal.UsersTab;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.core.remoting.RemoteTLEGroupService;
import com.tle.core.remoting.RemoteTLEUserService;
import com.tle.core.remoting.RemoteUserService;

public class UserWrapper extends GeneralPlugin<UserManagementSettings>
{
	private UsersTab userPanel;

	public UserWrapper()
	{
		super();
	}

	@Override
	public void init()
	{
		super.init();
		userPanel = new UsersTab(clientService.getService(RemoteTLEUserService.class),
			clientService.getService(RemoteTLEGroupService.class), clientService.getService(RemoteUserService.class));
		addFillComponent(userPanel);
	}

	@Override
	public boolean hasSave()
	{
		return false;
	}

	@Override
	public void load(UserManagementSettings settings)
	{
		// Nothing to see here, move along...
	}

	@Override
	public boolean save(UserManagementSettings settings) throws EditorException
	{
		userPanel.save();
		return true;
	}

	@Override
	public String getDocumentName()
	{
		return "user";
	}
}
