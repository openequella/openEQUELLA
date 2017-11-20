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

package com.tle.web.workflow.myresources;

import java.util.Arrays;
import java.util.List;

import com.tle.beans.item.ItemStatus;
import com.tle.core.guice.Bind;
import com.tle.web.search.filter.FilterByItemStatusSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;

@Bind
public class ModerationQueueStatusFilter extends FilterByItemStatusSection
{
	@TreeLookup
	private FilterByItemStatusSection otherFilter;

	@Override
	protected List<ItemStatus> getStatusList()
	{
		return Arrays.asList(ItemStatus.MODERATING, ItemStatus.REVIEW, ItemStatus.REJECTED);
	}

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		itemStatus.setParameterId("mstatus");
		onlyInModeration.setParameterId("msmodonly");
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model(true);
	}

	public void enable(SectionInfo info)
	{
		getModel(info).setDisabled(false);
		otherFilter.disable(info);
	}
}
