package com.tle.web.workflow.manage;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.TaskModerator;
import com.tle.core.services.entity.WorkflowService;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
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
	@PlugKey("tasklist.moderators")
	private static String KEY_MODERATORS;
	@PlugKey("tasklist.comments")
	private static String KEY_COMMENTS;
	@PlugKey("tasklist.summary")
	private static Label LABEL_SUMMARY;

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

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(moderatorsDialog, id);
		tree.registerInnerSection(commentsDialog, id);
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
		@Inject
		private WorkflowService workflowService;

		private TaskManagementItemList itemList;

		@Override
		public HtmlLinkState getTitle()
		{
			return itemList.getSummaryLink(info, getTitleLabel(), getItemTaskId(), true);
		}

		@Override
		protected void setupMetadata(RenderContext context)
		{
			super.setupMetadata(context);
			ItemTaskId itemTaskId = getItemTaskId();
			addRatingMetadata(itemList.getSummaryLink(info, LABEL_SUMMARY, itemTaskId, false));
			addRatingMetadata(itemList.getModeratorLink(info, itemTaskId));
			List<WorkflowMessage> comments = workflowService.getCommentsForTask(itemTaskId);
			if( !comments.isEmpty() )
			{
				addRatingMetadata(itemList.getCommentLink(info, itemTaskId, comments.size()));
			}
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
		return new HtmlLinkState(new PluralKeyLabel(KEY_MODERATORS, remaining), new OverrideHandler(
			moderatorsDialog.getOpenFunction(), itemTaskId));
	}

	public HtmlLinkState getCommentLink(SectionInfo info, ItemTaskId itemTaskId, int comments)
	{
		return new HtmlLinkState(new PluralKeyLabel(KEY_COMMENTS, comments), new OverrideHandler(
			commentsDialog.getOpenFunction(), itemTaskId));
	}

	public HtmlLinkState getSummaryLink(SectionInfo info, Label label, ItemTaskId itemTaskId, boolean progress)
	{
		return new HtmlLinkState(label, new BookmarkAndModify(info, events.getNamedModifier(
			"summary", ItemId.fromKey(itemTaskId), progress))); //$NON-NLS-1$
	}
}
