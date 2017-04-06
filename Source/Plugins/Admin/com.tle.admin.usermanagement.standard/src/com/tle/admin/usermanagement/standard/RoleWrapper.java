/*
 * Created on Mar 21, 2005
 */
package com.tle.admin.usermanagement.standard;

import com.tle.admin.plugin.GeneralPlugin;
import com.tle.admin.usermanagement.role.RoleAssigner;
import com.tle.beans.usermanagement.standard.wrapper.RoleWrapperSettings;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteUserService;

public class RoleWrapper extends GeneralPlugin<RoleWrapperSettings>
{
	private RoleAssigner roleAssigner;

	@Override
	public void init()
	{
		roleAssigner = new RoleAssigner(clientService.getService(RemoteUserService.class));
		addFillComponent(roleAssigner);
	}

	@Override
	public void load(RoleWrapperSettings settings)
	{
		roleAssigner.setRoleMappings(settings.getRoles());
	}

	@Override
	public boolean save(RoleWrapperSettings settings)
	{
		settings.setRoles(roleAssigner.getRoleMappings());
		return true;
	}

	@Override
	public String getDocumentName()
	{
		return CurrentLocale.get("com.tle.admin.usermanagement.rolewrapper.docname"); //$NON-NLS-1$
	}
}
