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

package com.tle.admin.itemdefinition;

import javax.swing.JPanel;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.AbstractPluginService;

public abstract class AbstractExtensionConfigPanel extends JPanel
{
	private String KEY_PFX = AbstractPluginService.getMyPluginId(getClass()) + ".";

	protected String getString(String key)
	{
		return CurrentLocale.get(KEY_PFX+key);
	}

	protected ClientService clientService;

	public void setClientService(ClientService clientService)
	{
		this.clientService = clientService;
	}

	public abstract void load(String stagingId, ItemDefinition itemdef);

	public abstract void save(ItemDefinition itemdef);
}