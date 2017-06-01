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

package com.tle.admin.collection.tool;

import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.itemdefinition.ItemEditor;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.EntityPack;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.core.remoting.RemoteItemDefinitionService;

public class ItemDefinitionTool extends BaseEntityTool<ItemDefinition>
{
	public ItemDefinitionTool() throws Exception
	{
		super(ItemDefinition.class, RemoteItemDefinitionService.ENTITY_TYPE);
	}

	@Override
	protected RemoteAbstractEntityService<ItemDefinition> getService(ClientService client)
	{
		return client.getService(RemoteItemDefinitionService.class);
	}

	@Override
	protected BaseEntityEditor<ItemDefinition> createEditor(boolean readonly)
	{
		return new ItemEditor(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.gui.itemdefinitiontool.collection"); //$NON-NLS-1$
	}

	@Override
	protected String getErrorPath()
	{
		return "itemEditor"; //$NON-NLS-1$
	}

	@Override
	public BaseEntityLabel add(EntityPack<ItemDefinition> entity, boolean lockAfterwards)
	{
		entity.getEntity().getSlow().setId(0);
		return super.add(entity, lockAfterwards);
	}
}
