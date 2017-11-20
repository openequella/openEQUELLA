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
