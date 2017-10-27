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

import java.util.*;

import javax.inject.Inject;

import com.tle.beans.item.ItemTaskId;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.common.search.DefaultSearch;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.item.TaskResult;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.*;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.Bootstrap;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.BeforeEventsListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.*;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.fancybox.FancyBoxDialogRenderer;
import com.tle.web.workflow.tasks.comments.ModCommentRender;
import com.tle.web.workflow.tasks.dialog.ApproveDialog;
import com.tle.web.workflow.tasks.dialog.CommentDialog;
import com.tle.web.workflow.tasks.dialog.RejectDialog;

public class CurrentTaskSection extends AbstractPrototypeSection<CurrentTaskSection.TasksModel>
	implements
		HtmlRenderer,
		BeforeEventsListener
{
	@PlugKey("moderate.assigntome")
	private static Label LABEL_ASSIGNTOME;
	@PlugKey("moderate.cancelassign")
	private static Label LABEL_CANCELASSIGN;
	@PlugKey("moderate.unassigned")
	private static Label LABEL_UNASSIGNED;
	@PlugKey("moderate.assignedtome")
	private static Label LABEL_TOME;
	@PlugKey("moderate.acceptreceipt")
	private static Label ACCEPT_RECEIPT;
	@PlugKey("moderate.rejectreceipt")
	private static Label REJECT_RECEIPT;
	@PlugKey("moderate.listof")
	private static String KEY_LISTOF;
	@PlugKey("moderate.selected.listof")
	private static String KEY_TASK_LISTOF;

	@Component
	@Inject
	private ApproveDialog approveDialog;

	@Component
	@Inject
	private RejectDialog rejectDialog;

	@Component
	@Inject
	private CommentDialog commentDialog;

	private static final int MAX_MODS = 3;
	private static final int NAV_PREV = -1;
	private static final int NAV_NEXT = 1;
	private static final int NAV_TASKLIST = -2;
	private static final String KEY_SELECTED_TASKS = "selectedTasks";

	@Inject
	private ItemService itemService;
	@Inject
	private BundleCache bundleCache;
	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Inject
	private FreeTextService freetextService;
	@Inject
	private ModerationService moderationService;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private UserSessionService session;
	@Inject
	private WorkflowService workflowService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService institutionService;

	@Component
	@PlugKey("moderate.next")
	private Button nextButton;
	@Component
	@PlugKey("moderate.prev")
	private Button prevButton;
	@Component
	@PlugKey("moderate.list")
	private Button listButton;
	@Component
	@PlugKey("moderate.postcomment")
	private Button postButton;
	@Component
	@PlugKey("moderate.showallmods")
	private Button showAllModsButton;
	@Component
	private Button assignButton;

	public enum CommentType
	{
		REJECT, COMMENT, SHOW, ACCEPT
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "_tasks";
	}

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		approveDialog.setSuccessCallable(events.getSubmitValuesFunction("success"));
		rejectDialog.setSuccessCallable(events.getSubmitValuesFunction("success"));
		commentDialog.setSuccessCallable(events.getSubmitValuesFunction("success"));
		nextButton.setClickHandler(events.getNamedHandler("nav", NAV_NEXT));
		prevButton.setClickHandler(events.getNamedHandler("nav", NAV_PREV));
		listButton.setClickHandler(events.getNamedHandler("nav", NAV_TASKLIST).setValidate(false));
		assignButton.setClickHandler(events.getNamedHandler("assign"));
		showAllModsButton.setDisplayed(false);
		userLinkSection = userLinkService.register(tree, id);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
	}

	@EventHandlerMethod
	public void success(SectionInfo info, CommentType commentType) throws Exception
	{
		Label receipt = null;
		switch (commentType)
		{
			case ACCEPT:
				receipt = ACCEPT_RECEIPT;
				break;
			case REJECT:
				receipt = REJECT_RECEIPT;
				break;
			case COMMENT:
				break;
		}
		if (receipt != null)
		{
			receiptService.setReceipt(receipt);
			TaskListState taskState = getModel(info).getTaskState();
			int index = taskState.getIndex();
			List<ItemTaskId> selectedTaskList = session.getAttribute(KEY_SELECTED_TASKS);
			if( selectedTaskList != null )
			{
				if( selectedTaskList.size() == 1 || (index + 1) == selectedTaskList.size() )
				{
					session.removeAttribute(KEY_SELECTED_TASKS);
					nav(info, NAV_TASKLIST);
					return;
				}

				nav(info, 1);
				return;
			}
			nav(info, NAV_TASKLIST);
		}
	}

	@EventHandlerMethod
	public void assign(SectionInfo info) throws Exception
	{
		TasksModel model = getModel(info);
		TaskListState taskState = model.getTaskState();
		itemService.operation(taskState.getItemTaskId(), workflowFactory.assign(taskState.getTaskId()),
			workflowFactory.save());
		refreshState(info);
	}

	private void refreshState(SectionInfo info)
	{
		TasksModel model = getModel(info);
		TaskListState taskState = model.getTaskState();
		model.setTaskState(moderationService.refreshCurrentTask(info, taskState));
	}

	/**
	 * 
	 * @param info
	 * @param commentType
	 * @param step
	 * @param comment
	 * @param messageUuid
	 * @return Returns true if a redirect happens
	 */
	public void doComment(SectionInfo info, CommentType commentType, String step, String comment, String messageUuid)
	{
		TasksModel model = getModel(info);
		TaskListState taskState = model.getTaskState();
		String taskId = taskState.getTaskId();
		WorkflowOperation op = null;
		switch( commentType )
		{
			case ACCEPT:
				op = workflowFactory.accept(taskId, comment, messageUuid);
				break;
			case REJECT:
				op = workflowFactory.reject(taskId, comment, step, messageUuid);
				break;
			case COMMENT:
				op = workflowFactory.comment(taskId, comment, messageUuid);
				break;

			default:
				// otherwise we're just SHOW-ing
				break;
		}
		itemService.operation(taskState.getItemTaskId(), op, workflowFactory.save());
	}

	@EventHandlerMethod
	public void nav(SectionInfo info, int dir)
	{
		if( dir == NAV_TASKLIST )
		{
			info.forwardAsBookmark(RootTaskListSection.createForward(info));
		}
		else
		{
			try
			{
				tryNavigation(info, dir);
			}
			catch( NotModeratingStepException nmse )
			{
				tryNavigation(info, dir);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void tryNavigation(SectionInfo info, int dir)
	{
		TasksModel model = getModel(info);
		TaskListState taskState = model.getTaskState();
		int newIndex = taskState.getIndex() + dir;

		List<ItemTaskId> selectedTaskList = session.getAttribute(KEY_SELECTED_TASKS);
		if( selectedTaskList != null )
		{
			moderationService.moderate(info, ModerationService.VIEW_METADATA, selectedTaskList.get(newIndex), newIndex,
				selectedTaskList.size());
		}
		else
		{
			SectionInfo forward = RootTaskListSection.createForward(info);
			TaskListResultsSection resultsSection = forward.lookupSection(TaskListResultsSection.class);
			FreetextSearchEvent event = resultsSection.createSearchEvent(forward);
			forward.processEvent(event);
			DefaultSearch search = event.getFinalSearch();

			FreetextSearchResults<TaskResult> searchResults = freetextService.search(search, newIndex, 1);
			int totalResults = searchResults.getAvailable();
			if( totalResults == 0 )
			{
				info.forwardAsBookmark(forward);
				return;
			}
			if( searchResults.getCount() == 0 )
			{
				newIndex = totalResults - 1;
				searchResults = freetextService.search(search, newIndex, 1);
				totalResults = searchResults.getAvailable();
				if( totalResults == 0 )
				{
					info.forwardAsBookmark(forward);
					return;
				}
			}
			TaskResult taskResult = searchResults.getResultData(0);
			ItemTaskId itemTaskId = new ItemTaskId(taskResult.getItemIdKey(), taskResult.getTaskId());
			moderationService.moderate(info, ModerationService.VIEW_METADATA, itemTaskId, newIndex, totalResults);
		}
		return;
	}

	@SuppressWarnings("nls")
	private void setupModeratorList(SectionInfo info)
	{
		TasksModel model = getModel(info);
		List<HtmlLinkState> links;
		List<HtmlLinkState> allLinks = new ArrayList<HtmlLinkState>();

		WorkflowStep step = model.getTaskState().getCurrentStep();
		allLinks.addAll(userLinkSection.createLinks(info, step.getToModerate()));
		allLinks.addAll(userLinkSection.createRoleLinks(info, step.getRolesToModerate()));
		if( allLinks.size() > MAX_MODS )
		{
			links = allLinks.subList(0, MAX_MODS);
			DialogState moreDialog = new DialogState();
			moreDialog.setInline(true);
			moreDialog.setContents(viewFactory.createResult("allmods.ftl", this));
			FancyBoxDialogRenderer moreDialogRenderer = new FancyBoxDialogRenderer(moreDialog);
			model.addDialog(moreDialogRenderer);
			showAllModsButton.setClickHandler(info, moreDialogRenderer.createOpenFunction());
			showAllModsButton.show(info);
			model.setAllModerators(allLinks);
		}
		else
		{
			links = allLinks;
		}
		model.setModerators(links);
	}

	@Override
	@SuppressWarnings("nls")
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		CommentDialog commentDialog = context.lookupSection(CommentDialog.class);
		postButton.setClickHandler(context, new OverrideHandler(commentDialog.getOpenFunction()));

		TasksModel model = getModel(context);
		TaskListState taskState = model.getTaskState();
		int index = taskState.getIndex();
		int taskListSize = taskState.getListSize();
		if( taskState.isEditing() )
		{
			prevButton.disable(context);
			nextButton.disable(context);
		}
		else
		{
			if( index == 0 )
			{
				prevButton.disable(context);
			}
			if( (index + 1) >= taskListSize )
			{
				nextButton.disable(context);
			}
		}
		List<ItemTaskId> selectedTaskList = session.getAttribute(KEY_SELECTED_TASKS);

		setupModeratorList(context);
		setupAssigned(context);
		int commentSize = taskState.getCommentCount();
		/*
		if( commentSize != 1 )
		{
			showButton.setLabel(context, new KeyLabel(KEY_SHOWCOMMENTS, commentSize));
		}
		if( commentSize == 0 )
		{
			showButton.disable(context);
		}*/
		if( taskListSize != 1 )
		{
			if( selectedTaskList != null )
			{
				listButton.setLabel(context, new KeyLabel(KEY_TASK_LISTOF, (taskState.getIndex() + 1), taskListSize));
			}
			else
			{
				listButton.setLabel(context, new KeyLabel(KEY_LISTOF, (taskState.getIndex() + 1), taskListSize));
			}
		}
		model.setSubmittedBy(userLinkSection.createLink(context, taskState.getStatus().getOwnerId()));
		WorkflowStep currentStep = taskState.getCurrentStep();
		model.setTaskName(new BundleLabel(currentStep.getName(), bundleCache));
		model.setTaskDescription(new BundleLabel(currentStep.getDescription(), bundleCache));


		// getMessages or getCommentsForTask??
		Collection<WorkflowMessage> messages = workflowService.getMessages(taskState.getItemTaskId());
		//int numComments = messages.size();
		//model.setCommentHeading(new PluralKeyLabel(KEY_COMMENTS, numComments));
		model.setCommentsSize(messages.size());
		model.setComments(ModCommentRender.render(context, viewFactory, userLinkSection, fileSystemService, messages));
		Label receipt = receiptService.getReceipt();
		if( receipt != null )
		{
			context.preRender(Bootstrap.PRERENDER);
			model.setReceipt(receipt.getText());
		}

		GenericTemplateResult template = new GenericTemplateResult(
			viewFactory.createNamedResult(OneColumnLayout.UPPERBODY, "currenttask.ftl", this));
		if( model.getModalSection() != null )
		{
			return new CombinedTemplateResult(renderToTemplate(context, model.getModalSection().getSectionId()),
				template);
		}
		String rootId = context.getWrappedRootId(this);
		return new FallbackTemplateResult(template, renderToTemplate(context, rootId));
	}

	private void setupAssigned(RenderEventContext context)
	{
		TasksModel model = getModel(context);
		TaskListState taskState = model.getTaskState();
		WorkflowStep step = taskState.getCurrentStep();
		String assignedTo = step.getAssignedTo();
		Label assignedLabel = null;
		if( assignedTo != null && assignedTo.equals(CurrentUser.getUserID()) )
		{
			assignButton.setLabel(context, LABEL_CANCELASSIGN);
			assignedLabel = LABEL_TOME;
		}
		else
		{
			if( assignedTo == null )
			{
				assignedLabel = LABEL_UNASSIGNED;
			}
			else
			{
				model.setAssignedTo(userLinkSection.createLink(context, assignedTo));
			}
			assignButton.setLabel(context, LABEL_ASSIGNTOME);
		}
		if( assignedLabel != null )
		{
			HtmlLinkState assigned = new HtmlLinkState(assignedLabel);
			assigned.setDisabled(true);
			model.setAssignedTo(assigned);
		}
	}

	@Override
	public void beforeEvents(SectionInfo info)
	{
		if( getModel(info).getTaskState() == null )
		{
			MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
			minfo.removeTree(getTree());
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new TasksModel();
	}

	public static class TasksModel
	{
		@Bookmarked(parameter = "_taskState", supported = true)
		private TaskListState taskState;
		private SectionId modalSection;
		private HtmlLinkState submittedBy;
		private HtmlLinkState assignedTo;
		private List<HtmlLinkState> moderators;
		private List<HtmlLinkState> allModerators;
		private SectionRenderable dialogs;
		private Label taskDescription;
		private Label taskName;
		private String receipt;
		private SectionRenderable comments;
		private int commentsSize;

		public Label getTaskName()
		{
			return taskName;
		}

		public void addDialog(SectionRenderable renderable)
		{
			dialogs = CombinedRenderer.combineResults(dialogs, renderable);
		}

		public void setTaskName(Label taskName)
		{
			this.taskName = taskName;
		}

		public TaskListState getTaskState()
		{
			return taskState;
		}

		public void setTaskState(TaskListState taskState)
		{
			this.taskState = taskState;
		}

		public SectionId getModalSection()
		{
			return modalSection;
		}

		public void setModalSection(SectionId modalSection)
		{
			this.modalSection = modalSection;
		}

		public HtmlLinkState getSubmittedBy()
		{
			return submittedBy;
		}

		public void setSubmittedBy(HtmlLinkState submittedBy)
		{
			this.submittedBy = submittedBy;
		}

		public HtmlLinkState getAssignedTo()
		{
			return assignedTo;
		}

		public void setAssignedTo(HtmlLinkState assignedTo)
		{
			this.assignedTo = assignedTo;
		}

		public List<HtmlLinkState> getModerators()
		{
			return moderators;
		}

		public void setModerators(List<HtmlLinkState> moderators)
		{
			this.moderators = moderators;
		}

		public List<HtmlLinkState> getAllModerators()
		{
			return allModerators;
		}

		public void setAllModerators(List<HtmlLinkState> allModerators)
		{
			this.allModerators = allModerators;
		}

		public SectionRenderable getDialogs()
		{
			return dialogs;
		}

		public void setDialogs(SectionRenderable dialogs)
		{
			this.dialogs = dialogs;
		}

		public Label getTaskDescription()
		{
			return taskDescription;
		}

		public void setTaskDescription(Label taskDescription)
		{
			this.taskDescription = taskDescription;
		}

		public void setReceipt(String receipt)
		{
			this.receipt = receipt;
		}

		public String getReceipt()
		{
			return receipt;
		}

		public SectionRenderable getComments()
		{
			return comments;
		}

		public void setComments(SectionRenderable comments)
		{
			this.comments = comments;
		}

		public void setCommentsSize(int size)
		{
			this.commentsSize = size;
		}

		public int getCommentsSize()
		{
			return commentsSize;
		}
	}

	public void setupState(SectionInfo info, TaskListState state)
	{
		getModel(info).setTaskState(state);
	}

	public TaskListState getCurrentState(SectionInfo info)
	{
		return getModel(info).getTaskState();
	}

	public Button getNextButton()
	{
		return nextButton;
	}

	public Button getPrevButton()
	{
		return prevButton;
	}

	public Button getListButton()
	{
		return listButton;
	}

	public void setModal(SectionInfo info, SectionId sectionId)
	{
		getModel(info).setModalSection(sectionId);
	}

	public WorkflowStep getCurrentStep(SectionInfo info)
	{
		return getModel(info).getTaskState().getCurrentStep();
	}

	public Button getPostButton()
	{
		return postButton;
	}

	public Button getAssignButton()
	{
		return assignButton;
	}

	public Button getShowAllModsButton()
	{
		return showAllModsButton;
	}

	/*public Button getShowButton()
	{
		return showButton;
	}*/

}
