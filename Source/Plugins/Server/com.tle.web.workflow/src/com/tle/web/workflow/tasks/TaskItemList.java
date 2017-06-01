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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.WorkflowService;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonTrait;
import com.tle.web.sections.equella.search.event.SearchResultsListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.workflow.manage.ViewCommentsDialog;
import com.tle.web.workflow.tasks.TaskItemList.TaskItemListEntry;

@Bind
public class TaskItemList extends AbstractItemList<TaskItemListEntry, TaskItemList.Model>
	implements
		SearchResultsListener<FreetextSearchResultEvent>
{
	@Inject
	private ModerationService taskListService;
	@PlugKey("tasklist.moderate")
	private static Label LABEL_MODERATE;
	@PlugKey("tasklist.comments")
	private static String KEY_COMMENTS;
	@PlugKey("tasklist.progress")
	private static Label LABEL_PROGRESS;
	@EventFactory
	private EventGenerator events;

	@Inject
	private Provider<TaskItemListEntry> entryFactory;
	@Inject
	private ViewCommentsDialog commentsDialog;

	@Override
	public void registered(String id, SectionTree tree)
	{
		tree.registerInnerSection(commentsDialog, id);
		super.registered(id, tree);
	}

	@SuppressWarnings("nls")
	@Override
	protected Set<String> getExtensionTypes()
	{
		return Collections.singleton("task");
	}

	@Override
	protected TaskItemListEntry createItemListEntry(SectionInfo info, Item item, FreetextResult result)
	{
		TaskItemListEntry entry = entryFactory.get();
		Model model = getModel(info);
		entry.setItem(item);
		entry.setInfo(info);
		entry.setup(this, model.nextEntryOffset());
		return entry;
	}

	@EventHandlerMethod(preventXsrf = false)
	public void moderate(SectionInfo info, ItemTaskId taskId, int index, int maximumResults, String view)
	{
		taskListService.moderate(info, view, taskId, index, maximumResults);
	}

	@Override
	public void processResults(SectionInfo info, FreetextSearchResultEvent results)
	{
		Model model = getModel(info);
		model.setMaximum(results.getMaximumResults());
		model.setOffset(results.getOffset());
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model extends AbstractItemList.Model<TaskItemListEntry>
	{
		private int offset;
		private int maximum;
		private int entryOffset;

		public int getOffset()
		{
			return offset;
		}

		public void setOffset(int offset)
		{
			this.offset = offset;
		}

		public int getMaximum()
		{
			return maximum;
		}

		public void setMaximum(int maximum)
		{
			this.maximum = maximum;
		}

		public int nextEntryOffset()
		{
			return entryOffset++;
		}
	}

	@SuppressWarnings("nls")
	public SubmitValuesHandler getModerateHandler(SectionInfo info, TaskItemListEntry entry, String view)
	{
		Model model = getModel(info);
		int offset = model.getOffset() + entry.getOffset();
		int totalSize = model.getMaximum();
		return events.getSubmitValuesHandler("moderate", entry.getItemTaskId(), offset, totalSize, view);
	}

	public HtmlLinkState getCommentLink(SectionInfo info, ItemTaskId itemTaskId, int comments)
	{
		return new HtmlLinkState(new PluralKeyLabel(KEY_COMMENTS, comments), new OverrideHandler(
			commentsDialog.getOpenFunction(), itemTaskId));
	}

	@Bind
	public static class TaskItemListEntry extends AbstractTaskListEntry
	{
		@Inject
		private WorkflowService workflowService;

		private int offset;
		private TaskItemList taskItemList;

		@Override
		public HtmlLinkState getTitle()
		{
			return new HtmlLinkState(getTitleLabel(), new BookmarkAndModify(info, taskItemList.getModerateHandler(info,
				this, ModerationService.VIEW_SUMMARY).getModifier()));
		}

		@Override
		protected void setupMetadata(RenderContext context)
		{
			super.setupMetadata(context);
			ItemTaskId itemTaskId = getItemTaskId();
			addRatingMetadata(new HtmlLinkState(LABEL_PROGRESS, taskItemList.getModerateHandler(info, this,
				ModerationService.VIEW_PROGRESS)));
			List<WorkflowMessage> comments = workflowService.getCommentsForTask(itemTaskId);
			if( !comments.isEmpty() )
			{
				addRatingMetadata(taskItemList.getCommentLink(info, itemTaskId, comments.size()));
			}

			HtmlLinkState state = new HtmlLinkState(LABEL_MODERATE, taskItemList.getModerateHandler(info, this,
				ModerationService.VIEW_METADATA));
			addRatingAction(new ButtonRenderer(state).setTrait(ButtonTrait.SUCCESS).setIcon(Icon.THUMBS_UP));
		}

		public void setup(TaskItemList taskItemList, int offset)
		{
			this.taskItemList = taskItemList;
			this.offset = offset;
		}

		public int getOffset()
		{
			return offset;
		}
	}

}
