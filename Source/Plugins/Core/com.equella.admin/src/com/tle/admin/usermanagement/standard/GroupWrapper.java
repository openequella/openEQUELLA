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
