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

package com.tle.web.cloneormove.model;

import java.util.Collections;
import java.util.List;

import com.dytech.common.text.NumberStringComparator;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.i18n.TextBundle;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class ContributableCollectionsModel extends DynamicHtmlListModel<ItemDefinition>
{
	private final ItemDefinitionService itemdefService;
	private final BundleCache bundleCache;
	private static String KEY_PFX = AbstractPluginService.getMyPluginId(ContributableCollectionsModel.class)+".";


	public ContributableCollectionsModel(ItemDefinitionService itemdefService, BundleCache bundleCache)
	{
		this.itemdefService = itemdefService;
		this.bundleCache = bundleCache;
	}

	@Override
	protected Iterable<ItemDefinition> populateModel(SectionInfo info)
	{
		List<ItemDefinition> itemDefs = itemdefService.enumerateCreateable();
		Collections.sort(itemDefs, new NumberStringComparator<ItemDefinition>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String convertToString(ItemDefinition t)
			{
				return TextBundle.getLocalString(t.getName(), bundleCache, null, "");
			}
		});
		itemDefs.add(0, null);
		return itemDefs;
	}

	@Override
	protected Option<ItemDefinition> convertToOption(SectionInfo info, ItemDefinition itemDef)
	{
		if( itemDef == null )
		{
			return new NameValueOption<ItemDefinition>(
				new NameValue(CurrentLocale.get(KEY_PFX+"selectcollection.option.collection.none"), ""),
				null);
		}
		return new NameValueOption<ItemDefinition>(
			new BundleNameValue(itemDef.getName(), itemDef.getUuid(), bundleCache), itemDef);
	}
}
