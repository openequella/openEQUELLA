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

package com.tle.web.remoterepo.merlot;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.NameValue;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;

/**
 * @author Aaron
 */
public class MerlotFilterListModel extends DynamicHtmlListModel<NameValue>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(MerlotFilterListModel.class);

	private final MerlotFilterType filterType;

	public MerlotFilterListModel(MerlotFilterType filterType)
	{
		this.filterType = filterType;
	}

	@SuppressWarnings("nls")
	@Override
	protected Iterable<NameValue> populateModel(SectionInfo info)
	{
		final List<NameValue> vals = new ArrayList<NameValue>();
		vals.add(new BundleNameValue(RESOURCES.key("filter.all"), ""));
		vals.addAll(filterType.getValues(info));
		return vals;
	}
}