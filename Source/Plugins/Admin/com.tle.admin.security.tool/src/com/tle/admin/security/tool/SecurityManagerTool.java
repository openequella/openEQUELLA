package com.tle.admin.security.tool;

import java.util.Set;

import com.tle.admin.AdminTool;
import com.tle.admin.security.tree.SecurityTree;

public class SecurityManagerTool extends AdminTool
{
	private boolean allowEditing;
	private final String editPrivilege;

	public SecurityManagerTool()
	{
		editPrivilege = "EDIT_SECURITY_TREE"; //$NON-NLS-1$
	}

	@Override
	public void setup(Set<String> grantedPrivilges, String name)
	{
		allowEditing = grantedPrivilges.contains(editPrivilege);
	}

	@Override
	public void toolSelected()
	{
		SecurityTree tree = new SecurityTree(clientService, driver.getPluginService(), allowEditing);
		tree.showDialog(parentFrame);
	}
}
