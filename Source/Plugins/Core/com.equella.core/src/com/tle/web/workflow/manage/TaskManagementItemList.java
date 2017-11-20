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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.workflow.TaskModerator;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.workflow.manage.TaskManagementItemList.TaskManagementListEntry;
import com.tle.web.workflow.tasks.AbstractTaskListEntry;
import com.tle.web.workflow.view.CurrentModerationLinkSection;

@Bind
public class TaskManagementItemList
	extends
		AbstractItemList<TaskManagementListEntry, AbstractItemList.Model<TaskManagementListEntry>>
{
	public static final String DIV_PFX = "tml_";

	@PlugKey("tasklist.moderators")
	private static String KEY_MODERATORS;
	@PlugKey("tasklist.comments")
	private static String KEY_COMMENTS;
	@PlugKey("tasklist.summary")
	private static Label LABEL_SUMMARY;
	@PlugKey("tasklist.selecttask")
	private static Label LABEL_SELECT;
	@PlugKey("tasklist.unselecttask")
	private static Label LABEL_UNSELECT;

	@Inject
	private WorkflowService workflowService;
	@Inject
	private Provider<TaskManagementListEntry> entryProvider;

	@Inject
	private TaskModeratorsDialog moderatorsDialog;
	@Inject
	private ViewCommentsDialog commentsDialog;

	@Inject
	private ViewItemUrlFactory urlFactory;
	@EventFactory
	private EventGenerator events;
	@TreeLookup
	private TaskSelectionSection selectionSection;
	private JSCallable removeCall;
	private JSCallable selectCall;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(moderatorsDialog, id);
		tree.registerInnerSection(commentsDialog, id);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		selectCall = selectionSection.getUpdateSelection(tree, events.getEventHandler("selectTask"));
		removeCall = selectionSection.getUpdateSelection(tree, events.getEventHandler("unSelectTask"));
	}


	@SuppressWarnings("nls")
	@Override
	protected Set<String> getExtensionTypes()
	{
		return Collections.singleton("task");
	}

	@Override
	protected TaskManagementListEntry createItemListEntry(SectionInfo info, Item item, FreetextResult result)
	{
		TaskManagementListEntry entry = entryProvider.get();
		entry.setItem(item);
		entry.setInfo(info);
		entry.setItemList(this);
		return entry;
	}

	@EventHandlerMethod
	public void summary(SectionInfo info, ItemId itemId, boolean progress)
	{
		ViewItemUrl vurl = urlFactory.createItemUrl(info, itemId);
		SectionInfo vinfo = vurl.getSectionInfo();
		if( progress )
		{
			vinfo.lookupSection(CurrentModerationLinkSection.class).execute(vinfo);
		}
		vurl.forward(info);
	}

	@Bind
	public static class TaskManagementListEntry extends AbstractTaskListEntry
	{
		private TaskManagementItemList itemList;

		@Override
		public HtmlLinkState getTitle()
		{
			return itemList.getSummaryLink(info, getTitleLabel(), getItemTaskId(), true);
		}

		public void setItemList(TaskManagementItemList itemList)
		{
			this.itemList = itemList;
		}
	}

	public HtmlLinkState getModeratorLink(SectionInfo info, ItemTaskId itemTaskId)
	{
		List<TaskModerator> mods = workflowService.getModeratorList(itemTaskId, true);
		int remaining = 0;
		for( TaskModerator taskModerator : mods )
		{
			if( !taskModerator.isAccepted() )
			{
				remaining++;
			}
		}
		return new HtmlLinkState(new PluralKeyLabel(KEY_MODERATORS, remaining),
			new OverrideHandler(moderatorsDialog.getOpenFunction(), itemTaskId));
	}

	public HtmlLinkState getCommentLink(SectionInfo info, ItemTaskId itemTaskId, int comments)
	{
		return new HtmlLinkState(new PluralKeyLabel(KEY_COMMENTS, comments),
			new OverrideHandler(commentsDialog.getOpenFunction(), itemTaskId));
	}

	public HtmlLinkState getSummaryLink(SectionInfo info, Label label, ItemTaskId itemTaskId, boolean progress)
	{
		return new HtmlLinkState(label,
			new BookmarkAndModify(info, events.getNamedModifier("summary", ItemId.fromKey(itemTaskId), progress))); //$NON-NLS-1$
	}

	@EventHandlerMethod
	public void selectTask(SectionInfo info, ItemTaskId itemTaskId)
	{
		selectionSection.addSelection(info, itemTaskId);
		addAjaxDiv(info, itemTaskId);
	}

	@EventHandlerMethod
	public void unSelectTask(SectionInfo info, ItemTaskId itemTaskId)
	{
		selectionSection.removeSelection(info, itemTaskId);
		addAjaxDiv(info, itemTaskId);
	}

	private void addAjaxDiv(SectionInfo info, ItemTaskId itemId)
	{
		AjaxRenderContext renderContext = info.getAttributeForClass(AjaxRenderContext.class);
		if( renderContext != null )
		{
			renderContext.addAjaxDivs(DIV_PFX + itemId.toString());
		}
	}


	@Override
	protected void customiseListEntries(RenderContext context, List<TaskManagementListEntry> entries) {
		super.customiseListEntries(context, entries);

		for (TaskManagementListEntry entry : entries)
		{
			ItemTaskId itemTaskId = entry.getItemTaskId();
			entry.getTag().setElementId(new SimpleElementId(DIV_PFX + itemTaskId.toString()));
			entry.addRatingMetadata(getSummaryLink(context, LABEL_SUMMARY, itemTaskId, false));
			entry.addRatingMetadata(getModeratorLink(context, itemTaskId));
			List<WorkflowMessage> comments = workflowService.getCommentsForTask(itemTaskId);
			if( !comments.isEmpty() )
			{
				entry.addRatingMetadata(getCommentLink(context, itemTaskId, comments.size()));
			}
			ButtonRenderer selectButton;
			if( selectionSection.isSelected(context, itemTaskId) )
			{
				selectButton = new ButtonRenderer(new HtmlLinkState(LABEL_UNSELECT,
						new OverrideHandler(removeCall, itemTaskId))).showAs(ButtonType.UNSELECT);
				entry.setSelected(true);
			}
			else
			{
				selectButton = new ButtonRenderer(new HtmlLinkState(LABEL_SELECT,
						new OverrideHandler(selectCall, itemTaskId))).showAs(ButtonType.SELECT);
			}
			entry.addRatingAction(selectButton);
		}
	}
}
