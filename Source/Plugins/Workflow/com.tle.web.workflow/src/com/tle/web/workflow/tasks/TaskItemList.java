package com.tle.web.workflow.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonTrait;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
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

	@PlugKey("mytasklist.selecttask")
	private static Label LABEL_SELECT;
	@PlugKey("mytasklist.unselecttask")
	private static Label LABEL_UNSELECT;

	@EventFactory
	private EventGenerator events;

	@Inject
	private Provider<TaskItemListEntry> entryFactory;
	@Inject
	private ViewCommentsDialog commentsDialog;
	@Inject
	private UserSessionService session;

	@TreeLookup
	private ModerateSelectedButton selectionSection;

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
		session.removeAttribute("selectedTasks");
		taskListService.moderate(info, view, taskId, index, maximumResults);
	}

	@EventHandlerMethod(preventXsrf = false)
	public void moderateSelectedTask(SectionInfo info, ItemTaskId taskId, int maximumResults)
	{
		taskListService.moderate(info, ModerationService.VIEW_METADATA, taskId, 0, maximumResults);
	}

	@Override
	public void processResults(SectionInfo info, FreetextSearchResultEvent results)
	{
		Model model = getModel(info);
		model.setMaximum(results.getMaximumResults());
		model.setOffset(results.getOffset());

		List<ItemTaskId> selections = new ArrayList<ItemTaskId>(selectionSection.getSelections(info));
		if( !selections.isEmpty() )
		{
			SubmitValuesHandler handler = events.getSubmitValuesHandler("moderateSelectedTask", selections.get(0),
				selections.size());
			selectionSection.setupModerateSelectedHandler(handler);
			session.setAttribute("selectedTasks", selections);
		}
		else
		{
			selectionSection.setupModerateSelectedHandler(null);
			session.removeAttribute("selectedTasks");
		}
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
		return new HtmlLinkState(new PluralKeyLabel(KEY_COMMENTS, comments),
			new OverrideHandler(commentsDialog.getOpenFunction(), itemTaskId));
	}

	public ButtonRenderer getSelectButton(SectionInfo info, ItemTaskId itemTaskId)
	{
		HtmlLinkState link = new HtmlLinkState(LABEL_SELECT,
			new OverrideHandler(events.getNamedHandler("selectTask", itemTaskId)));
		if( selectionSection.isSelected(info, itemTaskId) )
		{
			link = new HtmlLinkState(LABEL_UNSELECT,
				new OverrideHandler(events.getNamedHandler("unSelectTask", itemTaskId)));
			return new ButtonRenderer(link).showAs(ButtonType.UNSELECT);
		}
		return new ButtonRenderer(link).showAs(ButtonType.SELECT);
	}

	@EventHandlerMethod
	public void selectTask(SectionInfo info, ItemTaskId itemTaskId)
	{
		selectionSection.addSelection(info, itemTaskId);
	}

	@EventHandlerMethod
	public void unSelectTask(SectionInfo info, ItemTaskId itemTaskId)
	{
		selectionSection.removeSelection(info, itemTaskId);
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
			return new HtmlLinkState(getTitleLabel(), new BookmarkAndModify(info,
				taskItemList.getModerateHandler(info, this, ModerationService.VIEW_SUMMARY).getModifier()));
		}

		@Override
		protected void setupMetadata(RenderContext context)
		{
			super.setupMetadata(context);
			ItemTaskId itemTaskId = getItemTaskId();
			addRatingMetadata(new HtmlLinkState(LABEL_PROGRESS,
				taskItemList.getModerateHandler(info, this, ModerationService.VIEW_PROGRESS)));
			List<WorkflowMessage> comments = workflowService.getCommentsForTask(itemTaskId);
			if( !comments.isEmpty() )
			{
				addRatingMetadata(taskItemList.getCommentLink(info, itemTaskId, comments.size()));
			}

			HtmlLinkState state = new HtmlLinkState(LABEL_MODERATE,
				taskItemList.getModerateHandler(info, this, ModerationService.VIEW_METADATA));
			addRatingAction(taskItemList.getSelectButton(context, itemTaskId),
				new ButtonRenderer(state).setTrait(ButtonTrait.SUCCESS).setIcon(Icon.THUMBS_UP));
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
