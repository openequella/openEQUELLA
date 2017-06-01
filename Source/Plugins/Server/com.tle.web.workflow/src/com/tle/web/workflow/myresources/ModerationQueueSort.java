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

import java.util.List;

import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SortField;
import com.tle.common.searching.SortField.Type;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.freetext.TasksIndexer;
import com.tle.web.myresources.MyResourcesSortSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.search.sort.SortOption;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@Bind
public class ModerationQueueSort extends AbstractSortOptionsSection<FreetextSearchEvent>
{
	@TreeLookup
	private MyResourcesSortSection otherSort;

	@PlugKey("listhead.submitted")
	private static Label LABEL_SUBMITTED;
	@PlugKey("listhead.lastaction")
	private static Label LABEL_LASTACTION;

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		sortOptions.setParameterId("modsort");
		reverse.setParameterId("modrs");
	}

	@SuppressWarnings("nls")
	@Override
	protected void addSortOptions(List<SortOption> sorts)
	{
		sorts.add(new SortOption(LABEL_SUBMITTED, "started", new SortField(TasksIndexer.FIELD_STARTWORKFLOW, true,
			Type.LONG)));
		sorts.add(new SortOption(LABEL_LASTACTION, "lastmod", new SortField(TasksIndexer.FIELD_LASTACTION, false,
			Type.LONG)));
		sorts.add(new SortOption(SortType.NAME));
		sorts.add(new SortOption(SortType.DATEMODIFIED));
		sorts.add(new SortOption(SortType.DATECREATED));
	}

	public void enable(SectionInfo info)
	{
		getModel(info).setDisabled(false);
		otherSort.disable(info);
	}

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		return "started"; //$NON-NLS-1$
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		SortOptionsModel model = new SortOptionsModel();
		model.setDisabled(true);
		return model;
	}
}
