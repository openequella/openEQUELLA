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

package com.tle.web.search.filter;

import com.tle.beans.item.ItemStatus;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.equella.ItemStatusKeys;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

public class ItemStatusListModel extends SimpleHtmlListModel<ItemStatus>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService.getResourceHelper(ItemStatusListModel.class);

	public ItemStatusListModel()
	{
		add(null);
	}

	@SuppressWarnings("nls")
	@Override
	protected Option<ItemStatus> convertToOption(ItemStatus obj)
	{
		if( obj == null )
		{
			return new KeyOption<ItemStatus>(RESOURCES.key("statusfilter.all"), "", null);
		}
		return new KeyOption<ItemStatus>(ItemStatusKeys.get(obj), obj.name().toLowerCase(), obj);
	}
}