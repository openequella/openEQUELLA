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

import com.tle.common.Check;
import com.tle.core.workflow.freetext.TasksIndexer;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.AbstractFilterByUserSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class FilterByModeratorSection extends AbstractFilterByUserSection<FreetextSearchEvent>
{
	@PlugKey("filter.mod.title")
	private static Label LABEL_TITLE;
	@PlugKey("filter.mod.dialog")
	private static Label LABEL_DIALOG_TITLE;

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		String userId = getSelectedUserId(info);
		if( !Check.isEmpty(userId) )
		{
			event.filterByTerm(false, TasksIndexer.FIELD_MODUSER, userId);
		}
	}

	@Override
	protected String getPublicParam()
	{
		return "mod";
	}

	@Override
	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public Label getDialogTitle()
	{
		return LABEL_DIALOG_TITLE;
	}

	@Override
	public String getAjaxDiv()
	{
		return "moderator";
	}
}
