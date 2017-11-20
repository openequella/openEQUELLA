/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
