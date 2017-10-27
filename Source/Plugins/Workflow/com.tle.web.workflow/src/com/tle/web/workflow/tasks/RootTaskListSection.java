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

package com.tle.web.workflow.tasks;

import java.util.Map;

import javax.inject.Inject;

import com.tle.beans.item.ItemTaskId;
import com.tle.web.navigation.TopbarLinkService;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.InfoCreator;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.render.Label;

public class RootTaskListSection extends ContextableSearchSection<RootTaskListSection.Model>
{
	public static final String URL = "/access/tasklist.do"; //$NON-NLS-1$
	private static final String PARAM_TASKID = "taskId"; //$NON-NLS-1$

	@Inject
	private ModerationService moderationService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private TopbarLinkService topbarLinkService;

	@PlugKey("error.notmoderating")
	private static Label LABEL_NOTMODERATING;
	@PlugKey("title")
	private static Label LABEL_TITLE;

	@Override
	protected String getSessionKey()
	{
		return "taskListContext"; //$NON-NLS-1$
	}

	@Override
	public Label getTitle(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	public static SectionInfo createForward(InfoCreator creator)
	{
		return creator.createForward(URL);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@DirectEvent
	public void moderate(SectionInfo info)
	{
		ItemTaskId taskId = getModel(info).getTaskId();
		if( taskId != null )
		{
			try
			{
				moderationService.moderate(info, ModerationService.VIEW_METADATA, taskId, 0, 1);
			}
			catch( NotModeratingStepException nmse )
			{
				receiptService.setReceipt(LABEL_NOTMODERATING);
			}
		}
	}

	public class Model extends ContextableSearchSection.Model
	{
		@Bookmarked(stateful = false, supported = true, parameter = PARAM_TASKID)
		private ItemTaskId taskId;

		public ItemTaskId getTaskId()
		{
			return taskId;
		}

		public void setTaskId(ItemTaskId taskId)
		{
			this.taskId = taskId;
		}
	}

	public static class TaskModifier implements BookmarkModifier
	{
		private ItemTaskId taskId;

		public TaskModifier(ItemTaskId taskId)
		{
			this.taskId = taskId;
		}

		@Override
		public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState)
		{
			bookmarkState.put(PARAM_TASKID, new String[]{taskId.toString()});
		}

	}

	public static Bookmark createModerateBookmark(InfoCreator creator, ItemTaskId itemTaskId)
	{
		SectionInfo info = createForward(creator);
		return new BookmarkAndModify(info, new TaskModifier(itemTaskId));
	}

	@DirectEvent
	public void updateTopbar(SectionInfo info)
	{
		topbarLinkService.clearCachedData();
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.TWO_COLUMN;
	}
}
