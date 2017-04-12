/*
 * Created on Mar 21, 2005
 */
package com.tle.admin.usermanagement.standard;

import com.tle.admin.plugin.GeneralPlugin;
import com.tle.admin.usermanagement.internal.GroupsTab;
import com.tle.beans.ump.UserManagementSettings;

public class GroupWrapper extends GeneralPlugin<UserManagementSettings>
{
	private GroupsTab groupPanel;

	public GroupWrapper()
	{
		super();
	}

	@Override
	public void init()
	{
		super.init();
		groupPanel = new GroupsTab(clientService);
		addFillComponent(groupPanel);
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
	public boolean save(UserManagementSettings settings)
	{
		groupPanel.save();
		return true;
	}

	@Override
	public String getDocumentName()
	{
		return "group";
	}
}
