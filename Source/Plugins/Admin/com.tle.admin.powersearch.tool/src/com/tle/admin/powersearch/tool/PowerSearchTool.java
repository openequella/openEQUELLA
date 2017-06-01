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

package com.tle.admin.powersearch.tool;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.powersearch.PowerSearchEditor;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.core.remoting.RemotePowerSearchService;

public class PowerSearchTool extends BaseEntityTool<PowerSearch>
{
	public PowerSearchTool() throws Exception
	{
		super(PowerSearch.class, RemotePowerSearchService.ENTITY_TYPE);
	}

	@Override
	protected RemoteAbstractEntityService<PowerSearch> getService(ClientService client)
	{
		return client.getService(RemotePowerSearchService.class);
	}

	@Override
	protected String getErrorPath()
	{
		return "powersearch";
	}

	@Override
	protected BaseEntityEditor<PowerSearch> createEditor(boolean readonly)
	{
		return new PowerSearchEditor(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.gui.powersearchtool.title");
	}

	@Override
	protected PowerSearch process(PowerSearch powersearch)
	{
		List<Long> itemdefs = driver.getClientService().getService(RemotePowerSearchService.class)
			.enumerateItemdefIds(powersearch.getId());

		List<ItemDefinition> fullItemdefs = new ArrayList<ItemDefinition>();
		powersearch.setItemdefs(fullItemdefs);
		for( Long itemdef : itemdefs )
		{
			ItemDefinition definition = new ItemDefinition();
			definition.setId(itemdef);
			fullItemdefs.add(definition);
		}
		return powersearch;
	}
}
