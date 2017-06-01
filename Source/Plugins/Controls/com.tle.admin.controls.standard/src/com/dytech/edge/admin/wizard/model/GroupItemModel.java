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

package com.dytech.edge.admin.wizard.model;

import java.util.Arrays;
import java.util.List;

import com.dytech.edge.admin.wizard.Contexts;
import com.dytech.edge.wizard.beans.control.GroupItem;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class GroupItemModel extends AbstractControlModel<GroupItem>
{
	private GroupItem groupItem;

	/**
	 * Constructs a new GroupItemModel.
	 */
	public GroupItemModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		groupItem = (GroupItem) wrappedObject;
	}

	@Override
	public List<?> getChildObjects()
	{
		return groupItem.getControls();
	}

	@Override
	public List<String> getContexts()
	{
		return Arrays.asList(Contexts.CONTEXT_PAGE);
	}

	@Override
	public boolean allowsChildren()
	{
		return true;
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		GroupItem gcontrol = getControl();
		if( LangUtils.isEmpty(gcontrol.getName()) || Check.isEmpty(gcontrol.getValue()) )
		{
			return CurrentLocale.get("groupitem.validation.empty");
		}
		return null;
	}
}
