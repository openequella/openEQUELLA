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

package com.tle.web.workflow.manage;

import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.core.i18n.BundleCache;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;

public class WorkflowListModel extends DynamicHtmlListModel<BaseEntityLabel>
{
	static
	{
		PluginResourceHandler.init(WorkflowListModel.class);
	}

	@PlugKey("query.all")
	private static Label LABEL_ALL;
	private static final String ALL_KEY = ""; //$NON-NLS-1$

	private final BundleCache bundleCache;
	private final WorkflowService workflowService;

	public WorkflowListModel(WorkflowService workflowService, BundleCache bundleCache)
	{
		this.workflowService = workflowService;
		this.bundleCache = bundleCache;
		setSort(true);
	}

	@Override
	protected Iterable<BaseEntityLabel> populateModel(SectionInfo info)
	{
		return workflowService.listManagable();
	}

	@Override
	protected Option<BaseEntityLabel> convertToOption(SectionInfo info, BaseEntityLabel obj)
	{
		return new LabelOption<BaseEntityLabel>(new BundleLabel(obj.getBundleId(), bundleCache), obj.getUuid(), obj);
	}

	@Override
	protected Option<BaseEntityLabel> getTopOption()
	{
		return new LabelOption<BaseEntityLabel>(LABEL_ALL, ALL_KEY, null);
	}

	@Nullable
	@Override
	public BaseEntityLabel getValue(SectionInfo info, @Nullable String value)
	{
		if( value == null || ALL_KEY.equals(value) )
		{
			return null;
		}
		return super.getValue(info, value);
	}
}